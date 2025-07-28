package com.study.odersystem.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.odersystem.common.dto.ResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

// 403 에러 딱 대
@Component
@Slf4j
public class JwtAuthorizationHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error(accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ResponseDto<?> err = ResponseDto.ofFailure(HttpServletResponse.SC_FORBIDDEN,"권한이 없습니다.");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(err);
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}
