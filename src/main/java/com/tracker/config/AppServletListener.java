package com.tracker.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Слушатель жизненного цикла приложения
 */
@WebListener
public class AppServletListener implements ServletContextListener {

    private ApplicationManager manager;

    /**
     *  Запуск процессов при старте приложения :
     *  -Инициализация контекста;
     *  -Создание необходимых таблиц БД;
     *  -Управление планировщиком
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Class.forName("org.postgresql.Driver");
            manager = new ApplicationManager();
            DataBaseInit.initDataBase(manager.getDataSource());
            sce.getServletContext().setAttribute("applicationManager", manager);
            ConfigLoader configLoader = manager.getConfigLoader();
            // Если конфиг включен,планировщик авто-закрытия задач запускается
            if (Boolean.parseBoolean(configLoader.get("scheduler.enable")))
                manager.startScheduler();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка старта приложения", e);
        }
    }

    /**
     *Запуск процессов перед завершением приложения :
     * -Закрывает DataSource
     * -Останавливает Планировщик
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (manager != null)
            manager.shutdown();
    }
}
