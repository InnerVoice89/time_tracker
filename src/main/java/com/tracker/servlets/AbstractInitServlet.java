package com.tracker.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tracker.config.ApplicationManager;
import com.tracker.dao.UserDao;
import com.tracker.exceptions.IllegalRequestException;
import com.tracker.security.authentication.AuthService;
import com.tracker.services.UserService;
import com.tracker.services.TaskService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Базовый сервлет, содержащий общую инициализацию зависимостей приложения
 * и вспомогательные методы для формирования HTTP-ответов.
 */
public abstract class AbstractInitServlet extends HttpServlet {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected ObjectMapper objectMapper;
    protected ApplicationManager applicationManager;
    protected UserService userService;
    protected UserDao userDao;
    protected AuthService authService;
    protected DataSource dataSource;
    protected TaskService taskService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        applicationManager = (ApplicationManager) config.getServletContext().getAttribute("applicationManager");
        if (applicationManager == null) {
            throw new ServletException("ApplicationManager не инициализирован");
        }
        dataSource = applicationManager.getDataSource();
        userDao = applicationManager.getUserDao();
        userService = applicationManager.getUserService();
        authService = applicationManager.getAuthService();
        taskService = applicationManager.getTaskService();

    }

    /**
     * Формирует JSON-ответ.
     */
    protected void writeResponse(HttpServletResponse response, String contentType, int status,
                                 Object body) throws IOException {
        response.setStatus(status);
        response.setContentType(contentType);
        String responseBody = objectMapper.writeValueAsString(body);
        response.getWriter().write(responseBody);
    }

    /**
     * Извлекает идентификатор из pathInfo запроса.
     */
    protected long getPathId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/"))
            throw new IllegalRequestException("Некорректный запрос");
        return Long.parseLong(pathInfo.substring(1));
    }
}
