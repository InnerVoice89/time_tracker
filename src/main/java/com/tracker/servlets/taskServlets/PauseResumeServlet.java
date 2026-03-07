package com.tracker.servlets.taskServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/pause/*")
public class PauseResumeServlet extends AbstractInitServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long taskId = getPathInfo(req);
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
