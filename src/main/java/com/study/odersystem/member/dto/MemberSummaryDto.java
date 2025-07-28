package com.study.odersystem.member.dto;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryDto {
    private Long id;
    private String name;
    private String email;

    public static MemberSummaryDto fromEntity(Member member) {
        return MemberSummaryDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
