package com.study.odersystem.member.dto;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.domain.Role;
import com.study.odersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Boolean deleted;
    private List<Ordering> orderings = new ArrayList<>();

    public static MemberDetailDto fromEntity(Member member) {
        return MemberDetailDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .deleted(member.getDeleted())
                .orderings(member.getOrderings())
                .build();
    }
}
