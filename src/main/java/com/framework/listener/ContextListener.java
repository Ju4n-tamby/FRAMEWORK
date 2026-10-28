package com.framework.listener;

import com.framework.model.UrlMapping;
import com.framework.model.UrlMethod;
import com.framework.service.AuthConfig;
import com.framework.service.UploadConfig;
import com.framework.service.Utils;
import com.framework.service.ViewPath;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.HashMap;
import java.util.Map;

@WebListener
public class ContextListener implements ServletContextListener {
    Map<UrlMethod, UrlMapping> urlMappings = new HashMap<UrlMethod, UrlMapping>();

    public void contextInitialized(ServletContextEvent event) {
        String controller = event.getServletContext().getInitParameter("controller");
        try {
            this.urlMappings = Utils.getMappingsAvecMethod(controller);
            event.getServletContext().setAttribute("urlMappings", this.urlMappings);
            String prefix = event.getServletContext().getInitParameter("prefix");
            String suffix = event.getServletContext().getInitParameter("suffix");
            event.getServletContext().setAttribute("viewPath", new ViewPath(prefix, suffix));

            UploadConfig.load(event.getServletContext());

            AuthConfig.load(event.getServletContext());
            if (!AuthConfig.isConfigured()) {
                for (UrlMapping mapping : this.urlMappings.values()) {
                    if (Utils.estProtege(mapping.getMethod())) {
                        throw new IllegalStateException("@Auth utilisé sur " + mapping
                                + " mais le context-param \"" + AuthConfig.PARAM_SESSION + "\" est absent du web.xml");
                    }
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException("Erreur lors de l initialisation du contexte", exception);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().removeAttribute("urlMappings");
        event.getServletContext().removeAttribute("viewPath");
    }
}
