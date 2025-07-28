package com.study.odersystem.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.odersystem.common.dto.ResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

// 401에러 딱 대
@Slf4j
@Component
public class JwtAuthenticationHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error(authException.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 헤더에 상태코드 세팅
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ResponseDto<?> err = ResponseDto.ofFailure(HttpServletResponse.SC_UNAUTHORIZED, "token이 없거나 유효하지 않습니다.");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(err);

        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}