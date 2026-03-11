package com.tracker.servlets.taskServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Сервлет для удаления всех задач пользователя
 */
@WebServlet("/api/admin/clean/*")
public class CleanTasksServlet extends AbstractInitServlet {
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long userId = getPathId(req);
            log.info("Запрос на удаление задач пользователя с Id {}", userId);
            taskService.cleanAllTasksByUserId(userId);
            writeResponse(resp, "application/json", 200, BaseResponse.builder()
                    .success(true)
                    .message("Удаление задач прошло успешно")
                    .build());
        } catch (Exception e) {
            log.warn("Произошла ошибка при удалении задач", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(false)
                    .error("Произошла ошибка при удалении задач :" + e.getMessage())
                    .build());
        }
    }
}
