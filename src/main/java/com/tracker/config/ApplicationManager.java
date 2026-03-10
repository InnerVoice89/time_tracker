package com.tracker.config;

import com.tracker.dao.TaskDao;
import com.tracker.dao.UserDao;
import com.tracker.security.authentication.AuthService;
import com.tracker.services.UserService;
import com.tracker.services.TaskService;
import com.tracker.services.TaskServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import javax.sql.DataSource;

/**
 * Класс-контекст приложения
 */
@Getter
public class ApplicationManager {

    private final DataSource dataSource;
    private final TaskAutoCloseScheduler taskAutoCloseScheduler;
    private final UserService userService;
    private final UserDao userDao;
    private final AuthService authService;
    private final TaskService taskService;
    private final TaskDao taskDao;
    private final ConfigLoader configLoader;
    private final TransactionManager transactionManager;

    public ApplicationManager() {
        ConnectionConfig config = new ConnectionConfig();
        configLoader = new ConfigLoader("application.properties");
        dataSource = config.dataSource(configLoader);
        userDao = new UserDao();
        transactionManager=new TransactionManager(dataSource);
        userService = new UserService(userDao, transactionManager);
        authService = new AuthService(userService);
        taskDao = new TaskDao();
        taskAutoCloseScheduler = new TaskAutoCloseScheduler(transactionManager);
        taskService = new TaskServiceImpl(taskDao,transactionManager);

    }

    /**
     * Запускает планировщик проверки незавершённых задач.
     */
    public void startScheduler() {
        taskAutoCloseScheduler.startScheduler();
    }
    /**
     * Останавливает планировщик проверки незавершённых задач.
     */
    public void shutdown() {
        if (dataSource != null && dataSource instanceof HikariDataSource)
            ((HikariDataSource) dataSource).close();
        taskAutoCloseScheduler.shutdown();
    }

}
