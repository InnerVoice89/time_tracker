package com.tracker.servlets.userServlets;

import com.tracker.dto.BaseResponse;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/admin/delete-user/*")
public class DeleteUserServlet extends AbstractInitServlet {
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = getPathInfo(req);
            log.info("Удаление пользователя с ID {}", id);
            adminService.deleteUser(id);
            writeResponse(resp, "application/json", 200, BaseResponse.builder()
                    .success(true)
                    .build()
            );
        } catch (Exception e) {
            log.error("Ошибка удаления пользователя", e);
            writeResponse(resp, "application/json", 400, BaseResponse.builder()
                    .success(false)
                    .error("Ошибка удаления пользователя :" + e.getMessage())
                    .build());

        }
    }
}
