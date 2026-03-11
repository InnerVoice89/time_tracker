package com.tracker.servlets.taskServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.dto.ShowTaskInfoRs;
import com.tracker.dto.TasksInPeriodRq;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
/**
 * Сервлет для получения информации о задачах пользователя за определенный период.
 * Метод POST принимает JSON с параметрами:
 * {
 *   "userId": 1,
 *   "periodStart": "2025-02-01T00:00:00",
 *   "periodEnd": "2025-05-04T00:00:00"
 * }
 * Возвращает JSON с результатом:
 * {
 *   "success": true,
 *   "data": {
 *       "intervalTasks": [...],
 *       "totalDuration": "12 ч. 30 m. 0 sec."
 *   }
 * }
 * В случае ошибки возвращает JSON с полем <code>error</code>.
 */
@WebServlet("/api/admin/period")
public class PeriodServlet extends AbstractInitServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {

            TasksInPeriodRq request = objectMapper.readValue(req.getReader(), TasksInPeriodRq.class);
            log.info("Поступил запрос на получение информации по работе над задачами в период между {} и {}",
                    request.getPeriodStart(), request.getPeriodEnd());
            ShowTaskInfoRs tasks = taskService.findTasksInPeriod(request);
            writeResponse(resp, "application/json", 200, BaseResponse.builder()
                    .success(true)
                    .data(tasks)
                    .build());
        } catch (Exception e) {
            log.error("Не удалось получить данные ", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(false)
                    .error("Не удалось получить данные :" + e.getMessage())
                    .build());
        }
    }
}
