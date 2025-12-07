package com.surest.repository;

import com.surest.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID>, JpaSpecificationExecutor<Member> {
    boolean existsByEmail(String email);
}
