package com.tracker.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.config.ApplicationManager;
import com.tracker.dao.UserDao;
import com.tracker.security.authentication.AuthService;
import com.tracker.services.AdminService;
import com.tracker.services.TaskService;
import com.tracker.services.UserService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;

public abstract class AbstractInitServlet extends HttpServlet {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected ObjectMapper objectMapper;
    protected ApplicationManager applicationManager;
    protected AdminService adminService;
    protected UserService userService;
    protected UserDao userDao;
    protected AuthService authService;
    protected DataSource dataSource;
    protected TaskService taskService;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        objectMapper = new ObjectMapper();
        applicationManager = (ApplicationManager) config.getServletContext().getAttribute("applicationManager");
        dataSource = applicationManager.getDataSource();
        userDao = applicationManager.getUserDao();
        adminService = applicationManager.getAdminService();
        userService = applicationManager.getUserService();
        authService = applicationManager.getAuthService();
        taskService=applicationManager.

    }

    public void writeResponse(HttpServletResponse response, String contentType, int status,
                              Object body) throws IOException {
        response.setStatus(status);
        response.setContentType(contentType);
        String responseBody = objectMapper.writeValueAsString(body);
        response.getWriter().write(responseBody);
    }
}
