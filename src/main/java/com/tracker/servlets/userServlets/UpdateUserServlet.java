package com.tracker.servlets.userServlets;

import com.tracker.dto.User;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Сервлет изменения существующего пользователя
 * В теле опционально передаем изменяемые данные:
 * {
 *  * "username":"user",
 *  * "password":"user123",
 *  * "timeZone":"Europe/Moscow",
 *  * "roles":[
 *  * "USER"
 *  * ]
 *  * }
 */
@WebServlet("/api/admin/update-user")
public class UpdateUserServlet extends AbstractInitServlet {


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = objectMapper.readValue(req.getReader(), User.class);
        try {
            userService.updateUser(user);
            writeResponse(resp, "application/json", 200, Map.of("message",
                    "Обновление пользователя прошло успешно")
            );
        } catch (Exception e) {
            log.warn("Ошибка обновления пользователя", e);
            writeResponse(resp, "application/json", 400, Map.of("error", e.getMessage()));
        }
    }
}
