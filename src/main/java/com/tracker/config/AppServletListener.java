package com.tracker.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppServletListener implements ServletContextListener {

    private ApplicationManager manager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Class.forName("org.postgresql.Driver");
            manager = new ApplicationManager();
            ConnectionConfig.initDataBase(manager.getDataSource());
            sce.getServletContext().setAttribute("applicationManager", manager);
            manager.startScheduler();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка старта приложения", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (manager != null)
            manager.shutdown();
    }
}
