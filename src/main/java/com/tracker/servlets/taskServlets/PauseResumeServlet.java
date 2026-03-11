package com.tracker.servlets.taskServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Сервлет для постановки на паузу/возобновление задачи
 */
@WebServlet("/api/task/pause/*")
public class PauseResumeServlet extends AbstractInitServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long taskId = getPathId(req);
            taskService.putPauseResume(taskId);
            writeResponse(resp, "application/json", 200, BaseResponse.builder()
                    .success(true)
                    .build());
        } catch (Exception e) {
            log.warn("Произошла ошибка", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(false)
                    .error("Произошла ошибка :" + e.getMessage())
                    .build());

        }

    }
}
