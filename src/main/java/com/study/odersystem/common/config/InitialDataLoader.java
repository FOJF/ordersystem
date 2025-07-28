package com.study.odersystem.common.config;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.domain.Role;
import com.study.odersystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${jwt.adminEmail}")
    private String email;

    @Value("${jwt.adminPassword}")
    private String password;

    @Override
    public void run(String... args) throws Exception {
        if (memberRepository.findByEmail(email).isPresent()) return;

        Member member = Member.builder()
                .email(email)
                .role(Role.ADMIN)
                .password(passwordEncoder.encode(password))
                .build();

        memberRepository.save(member);
    }
}
