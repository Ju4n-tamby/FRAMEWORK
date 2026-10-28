package com.framework.service;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class AuthConfig {

    public static final String PARAM_SESSION = "authSession";

    private static volatile String sessionName;

    private AuthConfig() {
    }

    public static void load(ServletContext context) {
        String name = context.getInitParameter(PARAM_SESSION);
        sessionName = (name == null || name.isBlank()) ? null : name.trim();
    }

    public static boolean isConfigured() {
        return sessionName != null;
    }

    public static String getSessionName() {
        String name = sessionName;
        if (name == null) {
            throw new IllegalStateException("Authentification non configurée : ajouter le context-param \""
                    + PARAM_SESSION + "\" dans le web.xml");
        }
        return name;
    }

    public static boolean estConnecte(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(getSessionName()) != null;
    }
}
