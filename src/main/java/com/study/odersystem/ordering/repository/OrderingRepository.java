package com.study.odersystem.ordering.repository;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.ordering.domain.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    List<Ordering> findAllByMember(Member member);
}
