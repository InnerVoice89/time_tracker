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

            if (path.equals("/auth")) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            if (session == null || session.getAttribute("user") == null) {
                ResponseUtils.errorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Ошибка доступа");
                return;
            }
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
