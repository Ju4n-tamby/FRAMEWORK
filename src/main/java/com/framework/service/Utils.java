package com.framework.service;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
                // Running from exploded classes (e.g. IDE or exploded WAR)
                File directory = new File(packageURL.toURI());
                if (!directory.exists() || !directory.isDirectory()) {
                    throw new RuntimeException("Directory not found for package: " + packageName);
                }
                String[] files = directory.list();
                if (files == null) {
                    throw new RuntimeException("No classes found in package: " + packageName);
                }
                for (String file : files) {
                    if (file.endsWith(".class")) {
                        String className = packageName + '.' + file.substring(0, file.length() - 6);
                        classes.add(Class.forName(className));
                    }
                }

            } else if ("jar".equals(protocol)) {
                // Running from inside a JAR/WAR
                JarURLConnection jarConn = (JarURLConnection) packageURL.openConnection();
                JarFile jarFile = jarConn.getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    // Match entries that are directly in the package (no subdirectory)
                    if (entryName.startsWith(packagePath + "/")
                            && entryName.endsWith(".class")
                            && !entry.isDirectory()
                            && entryName.indexOf('/', packagePath.length() + 1) == -1) {
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
}