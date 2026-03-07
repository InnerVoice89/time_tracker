package com.tracker.servlets;

import com.tracker.dto.User;
import com.tracker.utils.ResponseUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/auth")
public class AuthServlet extends AbstractInitServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        User user = null;
        try {
            user = authService.authenticate(username, password);
        } catch (Exception ex) {
            ResponseUtils.errorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "Некорректные данные. " + ex.getMessage());
            return;
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
