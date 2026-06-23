package com.framework.controller;

import com.framework.model.UrlMapping;
import com.framework.model.UrlMethod;
import com.framework.service.Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FrontController extends HttpServlet {
    List<String> listeControllers = new ArrayList<>();
    private Map<UrlMethod, UrlMapping> mappings;

    @Override
    public void init() throws ServletException {
        try {
            String controllersPackage = getServletConfig().getInitParameter("controller");
            mappings = Utils.getMappingsAvecMethod(controllersPackage);

        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du DispatcherServlet", e);
        }
    }

    public void affichage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) {
            path = request.getServletPath();
        }

        String httpMethod = request.getMethod();
        UrlMethod urlMethod = new UrlMethod(httpMethod, path);

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        UrlMapping urlMapping = mappings.get(urlMethod);

        if (urlMapping != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.println("<h1>UrlMapping trouvé</h1>");
            out.println("<p><b>URL :</b> " + path + "</p>");
            out.println("<p><b>HttpMethod :</b>" + urlMethod.getMethod() + "</p>");
            out.println("<p><b>Controller :</b> " + urlMapping.getClazz().getName() + "</p>");
            out.println("<p><b>Méthode :</b> " + urlMapping.getMethod().getName() + "</p>");
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("<h1>404 - Page non trouvée</h1>");
            out.println("<p>L'URL <b>" + path + "</b> n'est pas prise en charge.</p>");
            out.println("<h2>URLs disponibles :</h2>");
            out.println("<ul>");
            for (UrlMethod method : mappings.keySet()) {
                out.println("<li><a href='" + request.getContextPath() + method.getUrl() + "'>" + method.getUrl() + "</a> → " + mappings.get(method) + "</li>");
            }
            out.println("</ul>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        affichage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        affichage(request, response);
    }
}
