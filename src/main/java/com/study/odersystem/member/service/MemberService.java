package com.study.odersystem.member.service;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.dto.MemberCreateDto;
import com.study.odersystem.member.dto.MemberDetailDto;
import com.study.odersystem.member.dto.LoginReqDto;
import com.study.odersystem.member.dto.MemberSummaryDto;
import com.study.odersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberDetailDto save(MemberCreateDto dto) {
        if (this.memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EntityExistsException("중복된 이메일입니다.");
        }

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        return MemberDetailDto.fromEntity(this.memberRepository.save(dto.toEntity()));
    }

    public MemberDetailDto doLogin(LoginReqDto dto) {
        Member member = this.memberRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 틀렸습니다."));
        if (!this.passwordEncoder.matches(dto.getPassword(), member.getPassword())) throw new BadCredentialsException("이메일 또는 비밀번호가 틀렸습니다.");
        if (member.getDeleted()) throw new EntityNotFoundException("삭제된 멤버입니다.");

        return MemberDetailDto.fromEntity(member);
    }

    public MemberDetailDto delete() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = this.memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 멤버"));
        member.delete();
        return MemberDetailDto.fromEntity(member);
    }

    public List<MemberSummaryDto> findAll() {
        return this.memberRepository.findAll().stream().map(MemberSummaryDto::fromEntity).collect(Collectors.toList());
    }

    public MemberDetailDto getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = this.memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 멤버"));
        return MemberDetailDto.fromEntity(member);
    }
}
