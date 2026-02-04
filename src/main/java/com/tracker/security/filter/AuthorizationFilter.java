package com.tracker.security.filter;


import com.tracker.dto.Role;
import com.tracker.models.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class AuthorizationFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String path = uri.substring(context.length());

        if (path.equals("/auth")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            unauthorized(response);
            return;
        }
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRoles() == null) {
            unauthorized(response);
            return;
        }
        if (path.startsWith("/admin") && !Role.has(Role.ADMIN,user.getRoles()))
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Нет прав доступа");
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);

    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Ошибка авторизации");
    }
}
