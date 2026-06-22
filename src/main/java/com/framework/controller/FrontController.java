package com.framework.controller;

import com.framework.model.Mapping;
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
    private List<String> listeControllers = new ArrayList<>();
    private Map<String, Mapping> mappings;

    @Override
    public void init() throws ServletException {
        try {
            String controllersPackage = getServletConfig().getInitParameter("controller");
            mappings = Utils.getMappings(controllersPackage);
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du FrontController", e);
        }
    }

    public void affichage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (path == null) path = request.getServletPath();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Mapping mapping = mappings.get(path);

        if (mapping != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.println("<h1>Mapping trouvé</h1>");
            out.println("<p><b>URL :</b> " + path + "</p>");
            out.println("<p><b>Controller :</b> " + mapping.getClassName() + "</p>");
            out.println("<p><b>Méthode :</b> " + mapping.getMethodName() + "</p>");
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
