package com.surest.service;

import com.surest.dto.MemberDTO;
import com.surest.dto.MemberSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MemberService {
    Page<MemberDTO> getAllMembers(int page, int size, String sort, String firstName, String lastName);

    // New
    Page<MemberDTO> searchMembers(MemberSearchRequest request, Pageable pageable);

    MemberDTO getMemberById(UUID id);

    MemberDTO createMember(MemberDTO dto);

    MemberDTO updateMember(UUID id, MemberDTO dto);

    void deleteMember(UUID id);
}
