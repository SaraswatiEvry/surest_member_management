package com.surest.controller;

import com.surest.dto.MemberDTO;
import com.surest.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public Page<MemberDTO> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        return memberService.getAllMembers(page, size, sort, firstName, lastName);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public MemberDTO getMemberById(@PathVariable UUID id) {
        return memberService.getMemberById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MemberDTO> createMember(@RequestBody @Valid MemberDTO dto) {
        return new ResponseEntity<>(memberService.createMember(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public MemberDTO updateMember(@PathVariable UUID id, @RequestBody @Valid MemberDTO dto) {
        return memberService.updateMember(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable UUID id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}