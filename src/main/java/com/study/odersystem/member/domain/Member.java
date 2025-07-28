package com.study.odersystem.member.domain;

import com.study.odersystem.common.domain.BaseTime;
import com.study.odersystem.ordering.domain.Ordering;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String name;
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private Boolean deleted = Boolean.FALSE;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private List<Ordering> orderings = new ArrayList<>();

    public void delete() {
        this.deleted = Boolean.TRUE;
    }
}
