package com.framework.service;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class AuthConfig {

    public static final String PARAM_SESSION = "authSession";
    public static final String PARAM_ROLE_SESSION = "roleSession";

    private static volatile String sessionName;
    private  static volatile String roleSession;

    private AuthConfig() {
    }

    public static void load(ServletContext context) {
        String name = context.getInitParameter(PARAM_SESSION);
        String role = context.getInitParameter(PARAM_ROLE_SESSION);
        sessionName = (name == null || name.isBlank()) ? null : name.trim();
        roleSession = (role == null || role.isBlank()) ? null : role.trim();
    }

    public static boolean isConfigured() {
        return sessionName != null;
    }

    public static boolean isRoleConfigured() {
        return roleSession != null;
    }

    public static String getSessionName() {
        String name = sessionName;
        if (name == null) {
            throw new IllegalStateException("Authentification non configurée : ajouter le context-param \""
                    + PARAM_SESSION + "\" dans le web.xml");
        }
        return name;
    }

    public static String getRoleSession() {
        String role = roleSession;
        if (role == null) {
            throw new IllegalStateException("Authentification non configurée : ajouter le context-param \""
                    + PARAM_ROLE_SESSION + "\" dans le web.xml");
        }
        return role;
    }

    public static boolean estConnecte(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(getSessionName()) != null;
    }

    public static boolean aleRole(HttpServletRequest request, String[] roles) {
        if (roles.length == 0) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object role = session.getAttribute(getRoleSession());
        if (role == null) {
            return false;
        }
        for (String r : roles) {
            if (r.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
