package com.tracker.utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseUtils {

    public static void errorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
    }
}
