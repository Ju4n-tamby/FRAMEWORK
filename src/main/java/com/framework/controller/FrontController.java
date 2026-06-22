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

    public void affichage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();

        if (path == null) {
            path = request.getServletPath();
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
