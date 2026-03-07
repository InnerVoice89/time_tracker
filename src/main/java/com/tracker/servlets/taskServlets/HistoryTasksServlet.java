package com.tracker.servlets.taskServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.dto.ShowTaskInfoRs;
import com.tracker.dto.TaskInfo;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/admin/tasks/history/*")
public class HistoryTasksServlet extends AbstractInitServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            long id = getPathInfo(req);
            log.info("Получение списка задач пользователя с ID {}", id);
            ShowTaskInfoRs taskList = taskService.getTasksByUserId(id);
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
