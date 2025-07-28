package com.study.odersystem.member.dto;

import com.study.odersystem.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryResDto {
    private Long id;
    private String name;
    private String email;

    public static MemberSummaryResDto fromEntity(Member member) {
        return MemberSummaryResDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
