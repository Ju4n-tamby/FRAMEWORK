package com.framework.controller;

import com.framework.model.UrlMapping;
import com.framework.model.UrlMethod;
import com.framework.model.VueData;
import com.framework.service.ViewPath;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class FrontController extends HttpServlet {
    private Map<UrlMethod, UrlMapping> mappings;
    private ViewPath viewPath;

    @Override
    public void init() throws ServletException {
        mappings = (HashMap<UrlMethod, UrlMapping>) getServletContext().getAttribute("urlMappings");

        // Récupère prefix/suffix depuis le web.xml (init-param du servlet)
        String prefix = getInitParameter("prefix");
        String suffix = getInitParameter("suffix");
        viewPath = new ViewPath(prefix, suffix);
        getServletContext().setAttribute("viewPath", viewPath);
    }

    public void affichage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) {
            path = request.getServletPath();
        }

        String httpMethod = request.getMethod();
        UrlMethod urlMethod = new UrlMethod(path, httpMethod);

        UrlMapping urlMapping = mappings.get(urlMethod);

        if (urlMapping != null) {
            try {
                Object controller = urlMapping.getClazz()
                        .getDeclaredConstructor()
                        .newInstance();

                Object retour = urlMapping.getMethod().invoke(controller);

                if (retour instanceof VueData) {
                    // ---- Cas VueData : on forward vers la JSP ----
                    VueData vueData = (VueData) retour;

                    if (vueData.getData() != null) {
                        for (Map.Entry<String, Object> entry : vueData.getData().entrySet()) {
                            request.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }

                    String viewName = viewPath.getPrefix() + vueData.getVue() + viewPath.getSuffix();
                    request.getRequestDispatcher(viewName).forward(request, response);

                } else {
                    // ---- Cas String / void : comportement debug actuel ----
                    response.setContentType("text/html;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_OK);
                    PrintWriter out = response.getWriter();

                    out.println("<h1>UrlMapping trouvé</h1>");
                    out.println("<p><b>URL :</b> " + path + "</p>");
                    out.println("<p><b>HttpMethod :</b>" + urlMethod.getMethod() + "</p>");
                    out.println("<p><b>Controller :</b> " + urlMapping.getClazz().getName() + "</p>");
                    out.println("<p><b>Méthode :</b> " + urlMapping.getMethod().getName() + "</p>");
                    out.println("<p><b>Retour :</b> " + retour + "</p>");
                }

            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else {
            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = response.getWriter();

            out.println("<h1>404 - Page non trouvée</h1>");
            out.println("<p>L'URL <b>" + path + "</b> n'est pas prise en charge.</p>");
            out.println("<h2>URLs disponibles :</h2>");
            out.println("<ul>");
            for (UrlMethod method : mappings.keySet()) {
                if (method.getMethod().equals("POST"))
                    out.println(
                            "<li><form action='" + request.getContextPath() + method.getUrl() + "' method='POST'>" +
                                    "<button type='submit'>" + method.getUrl() + "</button>" +
                                    " → " + mappings.get(method) +
                                    "</form></li>"
                    );
                else
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