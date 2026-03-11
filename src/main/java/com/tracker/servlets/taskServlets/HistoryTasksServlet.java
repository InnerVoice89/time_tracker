package com.tracker.servlets.taskServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.dto.ShowTaskInfoRs;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Сервлет для получения информации обо всех задачах пользователя
 */
@WebServlet("/api/admin/tasks/history/*")
public class HistoryTasksServlet extends AbstractInitServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            long userId = getPathId(req);
            log.info("Получение списка задач пользователя с ID {}", userId);
            ShowTaskInfoRs taskList = taskService.getTasksByUserId(userId);
            writeResponse(resp, "application/json", 200, BaseResponse.builder()
                    .success(true)
                    .data(taskList)
                    .build());
        } catch (Exception e) {
            log.error("шибка получения списка задач пользователя", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(false)
                    .error("Ошибка получения списка задач пользователя :" + e.getMessage())
                    .build());

        }
    }
}
