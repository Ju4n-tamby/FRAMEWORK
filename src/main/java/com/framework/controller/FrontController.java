package com.framework.controller;

<<<<<<< Updated upstream
=======
import com.framework.model.UrlMapping;
>>>>>>> Stashed changes
import com.framework.service.Utils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
<<<<<<< Updated upstream
import java.util.ArrayList;
import java.util.List;

public class FrontController extends HttpServlet {
    List<String> listeControllers = new ArrayList<>();
=======
import java.util.Map;

public class FrontController extends HttpServlet {
    private Map<String, UrlMapping> mappings;
>>>>>>> Stashed changes

    @Override
    public void init() throws ServletException {
        try {
            String controllersPackage = getServletConfig().getInitParameter("controller");

            listeControllers = Utils.getControllers(controllersPackage);
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du DispatcherServlet", e);
        }
    }

    public void affichage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();

<<<<<<< Updated upstream
        if (path == null) {
            path = request.getServletPath();
=======
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        UrlMapping urlMapping = mappings.get(path);

        if (urlMapping != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.println("<h1>UrlMapping trouvé</h1>");
            out.println("<p><b>URL :</b> " + path + "</p>");
            out.println("<p><b>Controller :</b> " + urlMapping.getClazz().getName() + "</p>");
            out.println("<p><b>Méthode :</b> " + urlMapping.getMethod().getName() + "</p>");
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("<h1>404 - Page non trouvée</h1>");
            out.println("<p>L'URL <b>" + path + "</b> n'est pas prise en charge.</p>");
            out.println("<h2>URLs disponibles :</h2>");
            out.println("<ul>");
            for (String url : mappings.keySet()) {
                out.println("<li><a href='" + request.getContextPath() + url + "'>" + url + "</a> → " + mappings.get(url) + "</li>");
            }
            out.println("</ul>");
>>>>>>> Stashed changes
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<h1>Bienvenue sur la page d'accueil</h1>");
        out.println("<p>URL actuelle : " + path + "</p>");

        out.println("<h2>Liste des controllers :</h2>");
        out.println("<ul>");
        for (String controllerName : listeControllers) {
            out.println("<li>" + controllerName + "</li>");
        }
        out.println("</ul>");
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
