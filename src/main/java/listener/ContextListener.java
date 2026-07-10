package listener;

import com.framework.model.UrlMapping;
import com.framework.model.UrlMethod;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.HashMap;
import java.util.Map;

@WebListener
public class ContextListener implements ServletContextListener {

    Map<UrlMethod, UrlMapping> urlMappings = new HashMap<UrlMethod, UrlMapping>();

    @Override
    public void contextInitialized(jakarta.servlet.ServletContextEvent sce) {
        String controllersPackage = sce.getServletContext().getInitParameter("controller");
        try {
            urlMappings = com.framework.service.Utils.getMappingsAvecMethod(controllersPackage);
            sce.getServletContext().setAttribute("urlMappings", urlMappings);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation du contexte", e);
        }
    }

    @Override
    public void contextDestroyed(jakarta.servlet.ServletContextEvent sce) {
        sce.getServletContext().removeAttribute("urlMappings");
    }
}
