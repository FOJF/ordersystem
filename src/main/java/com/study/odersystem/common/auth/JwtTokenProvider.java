package com.study.odersystem.common.auth;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    private final MemberRepository memberRepository;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.expirationAt}") // application.yml에 세팅해 놓은 값을 주입(깃헙에 올리지 않기 때문에 보안을 높일 수 있고, 코드가 방대할 때 손쉽게 변경 가능함)
    private int expirationAt;
    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;
    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key secret_at_key;
    private Key secret_rt_key;

    public JwtTokenProvider(MemberRepository memberRepository, @Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate) {
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    // 빈이 만들어진 직 후에 메서드가 바로 실행됨
    @PostConstruct
    public void init() {
        secret_at_key = new SecretKeySpec(Base64.getDecoder().decode(secretKeyAt), SignatureAlgorithm.HS512.getJcaName());
        secret_rt_key = new SecretKeySpec(Base64.getDecoder().decode(secretKeyRt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createAtToken(Member member) {
        String email = member.getEmail();
        String role = member.getRole().toString();

        // claims는 페이로드(사용자 정보)
        Claims claims = Jwts.claims().setSubject(email); // 메인 키(사용자의 정보)값은 set.subject
        claims.put("role", role); // 나머지 정보는 put을 사용

        Date now = new Date();

        // 발행 시간
        // 만료 시간
        // secret Key를 통해 signature 생성

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 발행 시간
                .setExpiration(new Date(now.getTime() + expirationAt * 60 * 1000L)) // 만료 시간
                .signWith(secret_at_key) // secret Key를 통해 signature 생성
                .compact();
    }

    public String createRtToken(Member member) {
        String email = member.getEmail();
        String role = member.getRole().toString();

        // claims는 페이로드(사용자 정보)
        Claims claims = Jwts.claims().setSubject(email); // 메인 키(사용자의 정보)값은 set.subject
        claims.put("role", role); // 나머지 정보는 put을 사용

        Date now = new Date();

        // 발행 시간
        // 만료 시간
        // secret Key를 통해 signature 생성
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 발행 시간
                .setExpiration(new Date(now.getTime() + expirationRt * 60 * 1000L)) // 만료 시간
                .signWith(secret_rt_key) // secret Key를 통해 signature 생성
                .compact();

        redisTemplate.opsForValue().set(email, token);
//        redisTemplate.opsForValue().set(email, token, 200, TimeUnit.DAYS); //200일 지나면 삭제? (ttl)

        return token;
    }

    public String refreshAtToken(String refreshToken) {
        // rt 그 자체를 검증
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeyRt)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
        String email = claims.getSubject();

        Member member = this.memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException(email));


        // redis의 값과 비교
        String redisRt = redisTemplate.opsForValue().get(member.getEmail());
        if (!redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("refresh token 유효안함");
        }

        return createAtToken(member);
    }
}
