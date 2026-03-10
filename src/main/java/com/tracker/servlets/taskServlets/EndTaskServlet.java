package com.tracker.servlets.taskServlets;

import com.beust.jcommander.ParameterException;
import com.tracker.dto.BaseResponse;
import com.tracker.exceptions.IllegalRequestException;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/stop/*")
public class EndTaskServlet extends AbstractInitServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long id= getPathId(req);
            log.info("Получение запроса на завершение задачи с Id - {}", id);
            taskService.endTask(id);
            writeResponse(resp, "application/json", 200, BaseResponse.builder()
                    .success(true)
                    .build());
        } catch (Exception e) {
            log.error("Произошла ошибка закрытия задания", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(true)
                    .error(e.getMessage())
                    .build());
        }
    }
}
