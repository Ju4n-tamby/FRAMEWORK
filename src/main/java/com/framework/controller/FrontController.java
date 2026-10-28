package com.framework.controller;

import com.framework.model.UrlMapping;
import com.framework.model.UrlMethod;
import com.framework.service.AuthConfig;
import com.framework.service.Utils;
import com.framework.service.ViewPath;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@MultipartConfig(maxFileSize=0x3200000L, maxRequestSize=0x6400000L)
public class FrontController
        extends HttpServlet {
    private Map<UrlMethod, UrlMapping> mappings;
    private ViewPath viewPath;

    public void init() throws ServletException {
        this.mappings = (HashMap)this.getServletContext().getAttribute("urlMappings");
        this.viewPath = (ViewPath)this.getServletContext().getAttribute("viewPath");
    }

    public void affichage(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String string;
        UrlMethod urlMethod;
        UrlMapping urlMapping;
        if (httpServletRequest.getDispatcherType() == DispatcherType.FORWARD) {
            RequestDispatcher requestDispatcher = this.getServletContext().getNamedDispatcher("jsp");
            requestDispatcher.forward((ServletRequest)httpServletRequest, (ServletResponse)httpServletResponse);
            return;
        }
        String string2 = httpServletRequest.getPathInfo();
        if (string2 == null) {
            string2 = httpServletRequest.getServletPath();
        }
        if ((urlMapping = this.mappings.get(urlMethod = new UrlMethod(string2, string = httpServletRequest.getMethod()))) != null) {
            try {
                Method method = urlMapping.getMethod();
                if (Utils.estProtege(method) && !AuthConfig.estConnecte(httpServletRequest)) {
                    httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Pas d'user");
                    return;
                }
                Object t = urlMapping.getClazz().getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                Object[] objectArray = Utils.construireArguments(method, httpServletRequest);
                Object object = method.invoke(t, objectArray);
                Utils.synchroniserSessions(objectArray, httpServletRequest);
                if (Utils.estRestAPI(method)) {
                    Utils.envoyerJson(object, httpServletResponse);
                }
                Utils.redirigerRequete(object, this.viewPath, httpServletRequest, httpServletResponse);
            }
            catch (Exception exception) {
                throw new ServletException((Throwable)exception);
            }
        } else {
            httpServletResponse.setContentType("text/html;charset=UTF-8");
            httpServletResponse.setStatus(404);
            PrintWriter printWriter = httpServletResponse.getWriter();
            printWriter.println("<h1>404 - Page non trouv\u00e9e</h1>");
            printWriter.println("<p>L'URL <b>" + string2 + "</b> n'est pas prise en charge.</p>");
            printWriter.println("<h2>URLs disponibles :</h2>");
            printWriter.println("<ul>");
            for (UrlMethod urlMethod2 : this.mappings.keySet()) {
                if (urlMethod2.getMethod().equals("POST")) {
                    printWriter.println("<li><form action='" + httpServletRequest.getContextPath() + urlMethod2.getUrl() + "' method='POST'><button type='submit'>" + urlMethod2.getUrl() + "</button> \u2192 " + String.valueOf(this.mappings.get(urlMethod2)) + "</form></li>");
                    continue;
                }
                printWriter.println("<li><a href='" + httpServletRequest.getContextPath() + urlMethod2.getUrl() + "'>" + urlMethod2.getUrl() + "</a> \u2192 " + String.valueOf(this.mappings.get(urlMethod2)) + "</li>");
            }
            printWriter.println("</ul>");
        }
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        this.affichage(httpServletRequest, httpServletResponse);
    }

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        this.affichage(httpServletRequest, httpServletResponse);
    }
}

