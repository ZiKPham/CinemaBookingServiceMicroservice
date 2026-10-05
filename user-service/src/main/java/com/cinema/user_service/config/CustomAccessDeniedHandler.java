package com.cinema.user_service.config;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Trả về một chuỗi JSON tùy chỉnh thông báo lỗi không đủ quyền
        String jsonResponse = "{"
                + "\"statusCode\": 403,"
                + "\"error\": \"Forbidden\","
                + "\"message\": \"Bạn không có quyền truy cập tài nguyên này!\","
                + "\"path\": \"" + request.getRequestURI() + "\""
                + "}";

        response.getWriter().write(jsonResponse);
    }

}
