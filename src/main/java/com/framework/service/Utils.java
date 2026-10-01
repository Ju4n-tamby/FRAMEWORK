package com.framework.service;

import com.framework.annotation.Controller;
import com.framework.annotation.FrontMapping;
import com.framework.annotation.Url;
import com.framework.model.UrlMapping;
import com.framework.model.UrlMethod;
import com.framework.model.VueData;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.net.JarURLConnection;

public class Utils {
    public static List<String> getControllers(String packageName) {
        List<Class<?>> classes = findClass(packageName);
        List<String> controllerNames = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(com.framework.annotation.Controller.class)) {
                controllerNames.add(clazz.getName());
            }
        }
        return controllerNames;
    }

    public static List<Class<?>> findClass(String packageName) {
        String packagePath = packageName.replace('.', '/');
        List<Class<?>> classes = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL packageURL = classLoader.getResource(packagePath);
            if (packageURL == null) {
                throw new RuntimeException("Package not found: " + packageName);
            }

            String protocol = packageURL.getProtocol();

            if ("file".equals(protocol)) {
                File directory = new File(packageURL.toURI());
                scanDirectory(directory, packageName, classes);

            } else if ("jar".equals(protocol)) {
                JarURLConnection jarConn = (JarURLConnection) packageURL.openConnection();
                JarFile jarFile = jarConn.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (entryName.startsWith(packagePath + "/")
                            && entryName.endsWith(".class")
                            && !entry.isDirectory()) {
                        String className = entryName.replace('/', '.').replace(".class", "");
                        classes.add(Class.forName(className));
                    }
                }

            } else {
                throw new RuntimeException("Unsupported protocol '" + protocol + "' for package: " + packageName);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error loading classes from package: " + packageName, e);
        }
        return classes;
    }

    private static void scanDirectory(File directory, String packageName, List<Class<?>> classes) throws Exception {
        if (!directory.exists() || !directory.isDirectory())
            return;

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
    }

    public static Map<String, UrlMapping> getMappings(String packageName) {
        List<Class<?>> classes = findClass(packageName);
        Map<String, UrlMapping> mappings = new HashMap<>();

        for (Class<?> clazz : classes) {
            if (!clazz.isAnnotationPresent(Controller.class))
                continue;

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(FrontMapping.class)) {
                    String url = method.getAnnotation(FrontMapping.class).value();
                    if (mappings.containsKey(url)) {
                        throw new RuntimeException(
                                "URL en double : '" + url + "' déjà mappée par " + mappings.get(url));
                    }
                    mappings.put(url, new UrlMapping(clazz, method));
                }
            }
        }
        return mappings;
    }

    public static Map<UrlMethod, UrlMapping> getMappingsAvecMethod(String packageName) {
        List<Class<?>> classes = findClass(packageName);
        Map<UrlMethod, UrlMapping> mappings = new HashMap<>();

        for (Class<?> clazz : classes) {
            if (!clazz.isAnnotationPresent(Controller.class))
                continue;

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Url.class)) {

                    Url annotation = method.getAnnotation(Url.class);

                    String url = annotation.value();
                    String httpMethod = annotation.method();

                    UrlMethod urlMethod = new UrlMethod(url, httpMethod);

                    if (mappings.containsKey(urlMethod)) {
                        throw new RuntimeException(
                                "URL en double : '" + url +
                                        "' déjà mappée par " + mappings.get(urlMethod));
                    }

                    mappings.put(urlMethod, new UrlMapping(clazz, method));
                }
            }
        }
        return mappings;
    }

    public static String formatterVue(String vue, ViewPath viewPath) {
        return viewPath.getPrefix() + vue + viewPath.getSuffix();
    }

    public static void redirigerRequete(Object result, ViewPath viewPath, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        if (result instanceof String) {
            String vue = formatterVue((String) result, viewPath);

            request.getRequestDispatcher(vue).forward(request, response);
        } else if (result instanceof VueData) {
            VueData vueData = (VueData) result;

            if (vueData.getData() != null) {
                for (Map.Entry<String, Object> entry : vueData.getData().entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
            }

            String vue = formatterVue(vueData.getVue(), viewPath);
            request.getRequestDispatcher(vue).forward(request, response);

        }
    }

    public static boolean estRestAPI(Method method) {
        return method.isAnnotationPresent(com.framework.annotation.RestAPI.class)
                || method.getDeclaringClass().isAnnotationPresent(com.framework.annotation.RestAPI.class);
    }

    public static void envoyerJson(Object object, HttpServletResponse response) throws java.io.IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        java.io.PrintWriter writer = response.getWriter();
        writer.write(toJson(object));
        writer.flush();
    }

    public static String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        serialize(obj, sb);
        return sb.toString();
    }

    private static void serialize(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String || obj instanceof Character) {
            sb.append('"').append(escape(obj.toString())).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
        } else if (obj instanceof Enum) {
            sb.append('"').append(escape(((Enum<?>) obj).name())).append('"');
        } else if (obj instanceof Map<?, ?> map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first)
                    sb.append(',');
                first = false;
                sb.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
                serialize(entry.getValue(), sb);
            }
            sb.append('}');
        } else if (obj instanceof Collection<?> collection) {
            sb.append('[');
            boolean first = true;
            for (Object item : collection) {
                if (!first)
                    sb.append(',');
                first = false;
                serialize(item, sb);
            }
            sb.append(']');
        } else if (obj.getClass().isArray()) {
            sb.append('[');
            int length = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                if (i > 0)
                    sb.append(',');
                serialize(java.lang.reflect.Array.get(obj, i), sb);
            }
            sb.append(']');
        } else {
            serializeBean(obj, sb);
        }
    }

    private static void serializeBean(Object obj, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic())
                    continue;
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (!first)
                        sb.append(',');
                    first = false;
                    sb.append('"').append(escape(field.getName())).append("\":");
                    serialize(value, sb);
                } catch (Exception ignored) {
                    // champ inaccessible, on l'ignore
                }
            }
            clazz = clazz.getSuperclass();
        }
        sb.append('}');
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static Object[] construireArguments(Method method, HttpServletRequest request) throws Exception {
        Parameter[] parametres = method.getParameters();
        Object[] arguments = new Object[parametres.length];

        for (int i = 0; i < parametres.length; i++) {
            Parameter p = parametres[i];
            Class<?> type = p.getType();

            if (estTypeSimple(type)) {
                String valeur = request.getParameter(p.getName());
                arguments[i] = convertir(valeur, type);
            } else {
                arguments[i] = construireObjet(type, request);
            }
        }
        return arguments;
    }

    private static boolean estTypeSimple(Class<?> type) {
        return type == String.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == float.class || type == Float.class
                || type == boolean.class || type == Boolean.class;

    }

    private static Object convertir(String valeur, Class<?> type) {
        if (valeur == null || valeur.isBlank()) {
            if (type == int.class)
                return 0;
            if (type == long.class)
                return 0L;
            if (type == double.class)
                return 0.0;
            if (type == float.class)
                return 0f;
            if (type == boolean.class)
                return false;
            return null;
        }
        if (type == String.class)
            return valeur;
        if (type == int.class || type == Integer.class)
            return Integer.parseInt(valeur);
        if (type == long.class || type == Long.class)
            return Long.parseLong(valeur);
        if (type == double.class || type == Double.class)
            return Double.parseDouble(valeur);
        if (type == float.class || type == Float.class)
            return Float.parseFloat(valeur);
        if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(valeur);
        return valeur;
    }

    private static Object construireObjet(Class<?> type, HttpServletRequest request) throws Exception {
        Object instance = type.getDeclaredConstructor().newInstance();
        for (Field field : type.getDeclaredFields()) {
            String valeur = request.getParameter(field.getName());
            if (valeur != null) {
                field.setAccessible(true);
                field.set(instance, convertir(valeur, field.getType()));
            }
        }
        return instance;
    }
}
