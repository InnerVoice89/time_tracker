package com.tracker.security.filter;


import com.tracker.UserContext;
import com.tracker.dto.Role;
import com.tracker.dto.User;
import com.tracker.utils.ResponseUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Фильтр аутентификации и авторизации пользователя.
 * Перехватывает все входящие HTTP-запросы и выполняет следующие проверки:
 * -Разрешает свободный доступ к эндпоинту аутентификации {@code /auth}.
 * -Проверяет наличие HTTP-сессии и авторизованного пользователя.
 * -Помещает пользователя из сессии в контекст приложения для дальнейшего
 * использования в сервисном слое.
 * -Проверяет права доступа для административных эндпоинтов.
 * -Если пользователь не авторизован или не имеет необходимых прав,
 * возвращается соответствующий HTTP-код ошибки.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            String uri = request.getRequestURI();
            String context = request.getContextPath();
            String path = uri.substring(context.length());
            HttpSession session = request.getSession(false);
            // Свободный доступ к сервису аутентификации
            if (path.equals("/auth")) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            if (session == null || session.getAttribute("user") == null) {
                ResponseUtils.errorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Ошибка доступа");
                return;
            }
            // Извлекаем из сессии пользователя и добавляем в контекст приложения для дальнейшего использования
            User user = (User) session.getAttribute("user");
            UserContext.setUser(user);
            if (path.startsWith("/api/admin") && !Role.has(Role.ADMIN, user.getRoles())) {
                ResponseUtils.errorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                        "Нет прав доступа");
                return;
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.clean();
        }
    }
}
