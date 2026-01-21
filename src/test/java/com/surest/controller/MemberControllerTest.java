
package com.surest.controller;

import com.surest.dto.MemberDTO;
import com.surest.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    @Test
    void testGetAllMembers() {
        MemberDTO member1 = new MemberDTO(UUID.randomUUID(), "John", "Doe",
                LocalDate.of(1990, 1, 1), "john@example.com", 1);
        MemberDTO member2 = new MemberDTO(UUID.randomUUID(), "Jane", "Smith",
                LocalDate.of(1985, 5, 15), "jane@example.com", 2);
        Page<MemberDTO> pageResult = new PageImpl<>(List.of(member1, member2));

        when(memberService.getAllMembers(anyInt(), anyInt(), anyString(), any(), any()))
                .thenReturn(pageResult);

        Page<MemberDTO> result = memberController.getAllMembers(0, 10, "lastName,asc", null, null);

        assertEquals(2, result.getContent().size());
        verify(memberService, times(1)).getAllMembers(eq(0), eq(10), eq("lastName,asc"), isNull(), isNull());
    }

    @Test
    void testGetMemberById() {
        UUID id = UUID.randomUUID();
        MemberDTO member = new MemberDTO(id, "John", "Doe",
                LocalDate.of(1990, 1, 1), "john@example.com", 1);

        when(memberService.getMemberById(id)).thenReturn(member);

        MemberDTO result = memberController.getMemberById(id);

        assertEquals("John", result.getFirstName());
        verify(memberService, times(1)).getMemberById(id);
    }

    @Test
    void testCreateMember() {
        MemberDTO dto = new MemberDTO(null, "Alice", "Brown",
                LocalDate.of(1992, 3, 10), "alice@example.com", 1);
        MemberDTO savedDto = new MemberDTO(UUID.randomUUID(), "Alice", "Brown",
                LocalDate.of(1992, 3, 10), "alice@example.com", 1);

        when(memberService.createMember(dto)).thenReturn(savedDto);

        ResponseEntity<MemberDTO> response = memberController.createMember(dto);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("Alice", response.getBody().getFirstName());
    }

    @Test
    void testUpdateMember() {
        UUID id = UUID.randomUUID();
        MemberDTO dto = new MemberDTO(id, "Updated", "Name",
                LocalDate.of(1995, 7, 20), "updated@example.com", 1);

        when(memberService.updateMember(id, dto)).thenReturn(dto);

        MemberDTO result = memberController.updateMember(id, dto);

        assertEquals("Updated", result.getFirstName());
    }

    @Test
    void testDeleteMember() {
        UUID id = UUID.randomUUID();
        doNothing().when(memberService).deleteMember(id);

        ResponseEntity<Void> response = memberController.deleteMember(id);

        assertEquals(204, response.getStatusCode().value());
    }
}
