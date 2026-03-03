package com.tracker.servlets.taskServlets;

import com.tracker.dto.RequestTaskDto;
import com.tracker.servlets.AbstractInitServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/api/start")
public class StartServlet extends AbstractInitServlet {

    private static final Logger log = LoggerFactory.getLogger(StartServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestTaskDto taskDto= new RequestTaskDto();
    }
}
