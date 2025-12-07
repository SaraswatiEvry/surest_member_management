package com.surest.service;

import com.surest.dto.MemberDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MemberService {
    Page<MemberDTO> getAllMembers(int page, int size, String sort, String firstName, String lastName);

    MemberDTO getMemberById(UUID id);

    MemberDTO createMember(MemberDTO dto);

    MemberDTO updateMember(UUID id, MemberDTO dto);

    void deleteMember(UUID id);
}
