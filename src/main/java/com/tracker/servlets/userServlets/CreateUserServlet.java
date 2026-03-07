package com.tracker.servlets.userServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.dto.User;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/admin/create-user")
public class CreateUserServlet extends AbstractInitServlet {

    private static final Logger log = LoggerFactory.getLogger(CreateUserServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User user = objectMapper.readValue(req.getReader(), User.class);
            adminService.createUser(user);
            writeResponse(resp, "application/json", 200,
                    BaseResponse.builder()
                            .success(true)
                            .message("Пользователь успешно создан")
                            .build());
        } catch (Exception e) {
            log.warn("Ошибка создания пользователя", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(false)
                    .error("Ошибка создания пользователя :" + e.getMessage())
                    .build());
        }
    }
}
