package com.framework.service;

import java.util.ArrayList;
import java.util.List;

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
            java.net.URL packageURL = classLoader.getResource(packagePath);
            if (packageURL == null) {
                throw new RuntimeException("Package not found: " + packageName);
            }
            java.io.File directory = new java.io.File(packageURL.toURI());
            if (directory.exists() && directory.isDirectory()) {
                String[] files = directory.list();
                if (files != null) {
                    for (String file : files) {
                        if (file.endsWith(".class")) {
                            String className = packageName + '.' + file.substring(0, file.length() - 6);
                            classes.add(Class.forName(className));
                        }
                    }
                } else {
                    throw new RuntimeException("No classes found in package: " + packageName);
                }
            } else {
                throw new RuntimeException("Directory not found for package: " + packageName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading classes from package: " + packageName, e);
        }
        return classes;
    }
}
