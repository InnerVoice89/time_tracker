package com.tracker.config;

import com.tracker.dao.TaskDao;
import com.tracker.dao.TaskDaoImpl;
import com.tracker.dao.UserDao;
import com.tracker.scheduler.SchedulerManager;
import com.tracker.security.authentication.AuthService;
import com.tracker.services.AdminService;
import com.tracker.services.TaskService;
import com.tracker.services.TaskServiceImpl;
import com.tracker.services.UserService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import javax.sql.DataSource;
import java.util.Properties;

@Getter
public class ApplicationManager {

    private final DataSource dataSource;
    private final SchedulerManager schedulerManager;
    private final AdminService adminService;
    private final UserService userService;
    private final UserDao userDao;
    private final AuthService authService;
    private final TaskService taskService;
    private final TaskDao taskDao;
    private final ConfigLoader configLoader;

    public ApplicationManager() {
        ConnectionConfig config = new ConnectionConfig();
        configLoader = new ConfigLoader("application.properties");
        dataSource = config.dataSource(configLoader);
        schedulerManager = new SchedulerManager(dataSource);
        userDao = new UserDao();
        adminService = new AdminService(userDao, dataSource);
        userService = new UserService(userDao, dataSource);
        authService = new AuthService(userService);
        taskDao = new TaskDaoImpl();
        taskService = new TaskServiceImpl(taskDao, dataSource);

    }


    public void startScheduler() {
        schedulerManager.startScheduler();
    }

    public void shutdown() {
        if (dataSource != null && dataSource instanceof HikariDataSource)
            ((HikariDataSource) dataSource).close();
        schedulerManager.shutdown();
    }

}
