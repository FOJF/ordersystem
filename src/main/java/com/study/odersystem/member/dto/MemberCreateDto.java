package com.study.odersystem.member.dto;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.domain.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateDto {
    @NotEmpty(message = "이름을 입력해주세요.")
    private String name;
    @NotEmpty(message = "이메일을 입력해주세요.")
    private String email;
    @NotEmpty(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자리 이상으로 입력해주세요.")
    private String password;

    @Builder.Default
    private Role role = Role.USER;

    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }
}
