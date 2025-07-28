package com.study.odersystem.member.controller;

import com.study.odersystem.common.auth.JwtTokenProvider;
import com.study.odersystem.common.dto.ResponseDto;
import com.study.odersystem.member.dto.*;
import com.study.odersystem.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@EnableMethodSecurity
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody MemberCreateDto memberCreateDto) {
        MemberDetailResDto dto = this.memberService.save(memberCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ofSuccess(dto, HttpStatus.CREATED.value(), "회원가입 성공"));
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody LoginReqDto loginReqDto) {
        MemberDetailResDto dto = this.memberService.doLogin(loginReqDto);
        String accessToken = jwtTokenProvider.createAtToken(dto);

        LoginResDto loginResDto = LoginResDto.builder()
                .accessToken(accessToken)
                .build();

        return ResponseEntity.ok(ResponseDto.ofSuccess(loginResDto, HttpStatus.OK.value(), "로그인 성공"));
    }

    @DeleteMapping()
    public ResponseEntity<?> delete() {
        MemberDetailResDto dto = this.memberService.delete();
        return ResponseEntity.accepted().body(ResponseDto.ofSuccess(dto, HttpStatus.ACCEPTED.value(), "정상적으로 탈퇴처리 되었습니다."));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll() {
        List<MemberSummaryResDto> dtos = this.memberService.findAll();
        return ResponseEntity.ok(ResponseDto.ofSuccess(dtos, HttpStatus.OK.value(), "유저 목록을 찾았습니다."));
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> getMyInfo() {
        MemberDetailResDto dto = this.memberService.getMyInfo();
        return ResponseEntity.ok(ResponseDto.ofSuccess(dto, HttpStatus.OK.value(), "내 정보 조회 성공"));
    }
}
