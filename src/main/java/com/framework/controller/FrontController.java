package com.framework.controller;

import com.framework.model.UrlMapping;
import com.framework.model.UrlMethod;
import com.framework.service.Utils;
import com.framework.service.ViewPath;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FrontController extends HttpServlet {
    private Map<UrlMethod, UrlMapping> mappings;
    private ViewPath viewPath;

    @Override
    public void init() throws ServletException {
        mappings = (HashMap<UrlMethod, UrlMapping>) getServletContext().getAttribute("urlMappings");
        viewPath = (ViewPath) getServletContext().getAttribute("viewPath");
    }

    public void affichage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getDispatcherType() == DispatcherType.FORWARD) {
            RequestDispatcher rd = getServletContext().getNamedDispatcher("jsp");
            rd.forward(request, response);
            return;
        }

        String path = request.getPathInfo();
        if (path == null) {
            path = request.getServletPath();
        }

        String httpMethod = request.getMethod();
        UrlMethod urlMethod = new UrlMethod(path, httpMethod);

        UrlMapping urlMapping = mappings.get(urlMethod);

        if (urlMapping != null) {
            try {
                Object controller = urlMapping.getClazz().getDeclaredConstructor().newInstance();
                Method method = urlMapping.getMethod();
                Object result = method.invoke(controller);

                if (Utils.estRestAPI(method)) {
                    Utils.envoyerJson(result, response);
                } else {
                    Utils.redirigerRequete(result, this.viewPath, request, response);
                }
            } catch (Exception exception) {
                throw new ServletException(exception);
            }
        } else {
            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = response.getWriter();

            out.println("<h1>404 - Page non trouvée</h1>");
            out.println("<p>L'URL <b>" + path + "</b> n'est pas prise en charge.</p>");
            out.println("<h2>URLs disponibles :</h2>");
            out.println("<ul>");
            for (UrlMethod m : mappings.keySet()) {
                if (m.getMethod().equals("POST"))
                    out.println(
                            "<li><form action='" + request.getContextPath() + m.getUrl() + "' method='POST'>" +
                                    "<button type='submit'>" + m.getUrl() + "</button>" +
                                    " → " + mappings.get(m) +
                                    "</form></li>");
                else
                    out.println("<li><a href='" + request.getContextPath() + m.getUrl() + "'>" + m.getUrl()
                            + "</a> → " + mappings.get(m) + "</li>");
            }
            out.println("</ul>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        affichage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        affichage(request, response);
    }
}