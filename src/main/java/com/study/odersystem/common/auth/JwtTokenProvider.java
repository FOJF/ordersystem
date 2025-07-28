package com.study.odersystem.common.auth;

import com.study.odersystem.member.dto.MemberDetailResDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expirationAt}") // application.yml에 세팅해 놓은 값을 주입(깃헙에 올리지 않기 때문에 보안을 높일 수 있고, 코드가 방대할 때 손쉽게 변경 가능함)
    private int expirationAt;

    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    private Key secret_at_key;

    // 빈이 만들어진 직 후에 메서드가 바로 실행됨
    @PostConstruct
    public void init() {
        secret_at_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyAt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createAtToken(MemberDetailResDto dto) {
        String email = dto.getEmail();
        String role = dto.getRole().toString();

        // claims는 페이로드(사용자 정보)
        Claims claims = Jwts.claims().setSubject(email); // 메인 키(사용자의 정보)값은 set.subject
        claims.put("role", role); // 나머지 정보는 put을 사용

        Date now = new Date();

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 발행 시간
                .setExpiration(new Date(now.getTime() + expirationAt * 60 * 1000L)) // 만료 시간
                .signWith(secret_at_key) // secret Key를 통해 signature 생성
                .compact();

        return token;
    }
}
