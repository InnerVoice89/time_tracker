package com.tracker.servlets.taskServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.dto.ShowTaskInfoRs;
import com.tracker.dto.TaskInfo;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/admin/task/*")
public class GetTaskServlet extends AbstractInitServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = getPathInfo(req);
            log.info("Получение информации по задаче - {}",id);
            ShowTaskInfoRs taskInfo = taskService.showTasksById(id);
            writeResponse(resp, "application/json", 200, BaseResponse.builder()
                    .success(true)
                    .data(taskInfo)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка получения информации по задаче", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(false)
                    .error("Ошибка получения информации по задаче : " + e.getMessage())
                    .build());
        }
    }
}
