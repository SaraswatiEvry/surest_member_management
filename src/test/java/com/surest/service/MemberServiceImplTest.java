
package com.surest.service;

import com.surest.dto.MemberDTO;
import com.surest.entity.Member;
import com.surest.exception.MemberAlreadyExistsException;
import com.surest.exception.MemberNotFoundException;
import com.surest.repository.MemberRepository;
import com.surest.util.MemberMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member member;
    private MemberDTO memberDTO;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        member = new Member();
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john@example.com");
        member.setDateOfBirth(LocalDate.of(1990, 1, 1));

        memberDTO = new MemberDTO(memberId, "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", 1);
    }

    @Test
    void testGetAllMembers() {
        Page<Member> memberPage = new PageImpl<>(List.of(member));
        when(memberRepository.findAll(
                ArgumentMatchers.<Specification<Member>> any(), any(Pageable.class)
        )).thenReturn(memberPage);
        when(memberMapper.toMemberDTO(member)).thenReturn(memberDTO);

        Page<MemberDTO> result = memberService.getAllMembers(0, 10, "lastName,asc", null, null);

        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());
        verify(memberRepository, times(1)).findAll(ArgumentMatchers.<Specification<Member>> any(), any(Pageable.class));
    }

    @Test
    void testGetMemberById_Success() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberMapper.toMemberDTO(member)).thenReturn(memberDTO);

        MemberDTO result = memberService.getMemberById(memberId);

        assertEquals("John", result.getFirstName());
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    void testGetMemberById_NotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.getMemberById(memberId));
    }

    @Test
    void testCreateMember_Success() {
        when(memberRepository.existsByEmail(memberDTO.getEmail())).thenReturn(false);
        when(memberMapper.toMemberEntity(memberDTO)).thenReturn(member);
        when(memberRepository.save(member)).thenReturn(member);
        when(memberMapper.toMemberDTO(member)).thenReturn(memberDTO);

        MemberDTO result = memberService.createMember(memberDTO);

        assertEquals("John", result.getFirstName());
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void testCreateMember_AlreadyExists() {
        when(memberRepository.existsByEmail(memberDTO.getEmail())).thenReturn(true);

        assertThrows(MemberAlreadyExistsException.class, () -> memberService.createMember(memberDTO));
        verify(memberRepository, never()).save(any());
    }

    @Test
    void testUpdateMember_Success() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);
        when(memberMapper.toMemberDTO(member)).thenReturn(memberDTO);

        MemberDTO result = memberService.updateMember(memberId, memberDTO);

        assertEquals("John", result.getFirstName());
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void testUpdateMember_NotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> memberService.updateMember(memberId, memberDTO));
    }

    @Test
    void testDeleteMember_Success() {
        when(memberRepository.existsById(memberId)).thenReturn(true);
        doNothing().when(memberRepository).deleteById(memberId);

        memberService.deleteMember(memberId);

        verify(memberRepository, times(1)).deleteById(memberId);
    }

    @Test
    void testDeleteMember_NotFound() {
        when(memberRepository.existsById(memberId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> memberService.deleteMember(memberId));
        verify(memberRepository, never()).deleteById(any());
    }
}
