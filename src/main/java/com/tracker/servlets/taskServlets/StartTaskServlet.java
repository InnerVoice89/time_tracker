package com.tracker.servlets.taskServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.util.Map;

/**
 * Сервлет для создания новой задачи
 * Принимает в запросе параметр:
 * POST /api/task/start?taskName=name
 * Возвращает идентификатор созданной задачи
 */
@WebServlet("/api/task/start")
public class StartTaskServlet extends AbstractInitServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {

            String taskName = req.getParameter("taskName");
            log.info("Получение запроса на старт задания {}", taskName);
            long taskId = taskService.startTask(taskName);
            writeResponse(resp, "application/json", 200, BaseResponse.builder()
                    .success(true)
                    .data(Map.of("taskId",taskId))
                    .build());
        } catch (Exception e) {
            log.error("Ошибка старта задания", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build());
        }
    }
}
