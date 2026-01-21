package com.surest.controller;

import com.surest.dto.MemberDTO;
import com.surest.dto.MemberSearchRequest;
import com.surest.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // (Optional) keep legacy GET for backward compatibility
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

    // New: POST search with PII-safe body DTO + pageable inheritance
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/search")
    public Page<MemberDTO> searchMembers(@Valid @RequestBody MemberSearchRequest request) {
        Pageable pageable = toPageable(request);
        return memberService.searchMembers(request, pageable);
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
        memberService.deleteMember(id); // throws MemberNotFoundException → handled globally → 404
        return ResponseEntity.noContent().build();
    }

    // --- helper ---
    private Pageable toPageable(MemberSearchRequest req) {
        Sort sort = Sort.unsorted();
        if (req.getSort() != null && !req.getSort().isEmpty()) {
            Sort combined = null;
            for (String s : req.getSort()) {
                String[] parts = s.split(",", 2);
                String property = parts[0].trim();
                String dir = parts.length > 1 ? parts[1].trim() : "asc";
                Sort next = "desc".equalsIgnoreCase(dir)
                        ? Sort.by(property).descending()
                        : Sort.by(property).ascending();
                combined = (combined == null) ? next : combined.and(next);
            }
            sort = combined;
        }
        return PageRequest.of(req.getPage(), req.getSize(), sort);
    }

}