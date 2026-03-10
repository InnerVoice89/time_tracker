package com.tracker.servlets.taskServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/admin/delete-task/*")
public class DeleteSpecTaskServlet extends AbstractInitServlet {


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            long taskId = getPathId(req);
            log.info("Запрос на удаление задачи {}", taskId);
            taskService.deleteTaskByTaskId(taskId);
            writeResponse(resp, "application/json", 200, BaseResponse.builder()
                    .success(true)
                    .message("Удаление задачи прошло успешно")
                    .build());
        } catch (Exception e) {
            log.warn("Произошла ошибка при удалении задачи ", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(false)
                    .error("Произошла ошибка при удалении задачи :" + e.getMessage())
                    .build());
        }
    }
}
