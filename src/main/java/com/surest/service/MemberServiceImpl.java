package com.surest.service;

import com.surest.dto.MemberDTO;
import com.surest.dto.MemberSearchRequest;
import com.surest.entity.Member;
import com.surest.exception.MemberAlreadyExistsException;
import com.surest.exception.MemberNotFoundException;
import com.surest.repository.MemberRepository;
import com.surest.util.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<MemberDTO> getAllMembers(int page, int size, String sort, String firstName, String lastName) {
        // Mask PII: only indicate if filters were provided, not their values
        log.info("Fetching members (legacy GET): page={}, size={}, sort={}, firstNameProvided={}, lastNameProvided={}",
                page, size, sort, firstName != null && !firstName.isBlank(), lastName != null && !lastName.isBlank());

        // Build pageable from legacy "field,direction" sort param
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim() : "asc";
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, property)));

        // Build dynamic specification using new combinators (no deprecated where())
        Specification<Member> spec = buildAndSpec(firstName, lastName);

        return memberRepository.findAll(spec, pageable).map(memberMapper::toMemberDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberDTO> searchMembers(MemberSearchRequest request, Pageable pageable) {
        log.info("Searching members: page={}, size={}, sort={}, firstNameProvided={}, lastNameProvided={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(),
                request.getFirstName() != null && !request.getFirstName().isBlank(),
                request.getLastName() != null && !request.getLastName().isBlank());

        Specification<Member> spec = buildAndSpec(request.getFirstName(), request.getLastName());
        return memberRepository.findAll(spec, pageable).map(memberMapper::toMemberDTO);
    }

    /**
     * AND all provided filters. Empty set -> match all.
     * Uses Specification.allOf(...) to avoid deprecated Specification.where(null).
     */
    private Specification<Member> buildAndSpec(String firstName, String lastName) {
        List<Specification<Member>> specs = new ArrayList<>();

        if (firstName != null && !firstName.isBlank()) {
            String fn = firstName.toLowerCase();
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("firstName")), "%" + fn + "%"));
        }
        if (lastName != null && !lastName.isBlank()) {
            String ln = lastName.toLowerCase();
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("lastName")), "%" + ln + "%"));
        }

        // All conditions combined with AND. If specs is empty, allOf(specs) == match all.
        return Specification.allOf(specs);
    }

    // If you prefer OR semantics instead, replace usages with buildOrSpec(...)
    @SuppressWarnings("unused")
    private Specification<Member> buildOrSpec(String firstName, String lastName) {
        List<Specification<Member>> specs = new ArrayList<>();

        if (firstName != null && !firstName.isBlank()) {
            String fn = firstName.toLowerCase();
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("firstName")), "%" + fn + "%"));
        }
        if (lastName != null && !lastName.isBlank()) {
            String ln = lastName.toLowerCase();
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("lastName")), "%" + ln + "%"));
        }

        // OR semantics; empty list => match none
        return Specification.anyOf(specs);
    }

    @Override
    @Cacheable(value = "members", key = "#id.toString()")
    @Transactional(readOnly = true)
    public MemberDTO getMemberById(UUID id) {
        log.info("Fetching member by ID: {}", id);
        return memberRepository.findById(id)
                .map(memberMapper::toMemberDTO)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Override
    @Transactional
    public MemberDTO createMember(MemberDTO dto) {
        log.info("Creating new member");
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new MemberAlreadyExistsException(dto.getEmail());
        }
        Member member = memberMapper.toMemberEntity(dto);
        Member saved = memberRepository.save(member);
        return memberMapper.toMemberDTO(saved);
    }

    @Transactional
    @Override
    @CachePut(value = "members", key = "#id.toString()") // refresh cache with updated entity
    public MemberDTO updateMember(UUID id, MemberDTO dto) {
        log.info("Updating member with ID: {}", id);
        Member existing = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        // Prefer mapper.updateEntityFromDto(dto, existing) if available.
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        existing.setDateOfBirth(dto.getDateOfBirth());

        Member saved = memberRepository.save(existing);
        return memberMapper.toMemberDTO(saved);
    }

    @Transactional
    @Override
    @CacheEvict(value = "members", key = "#id.toString()")
    public void deleteMember(UUID id) {
        log.info("Deleting member with ID: {}", id);
        if (!memberRepository.existsById(id)) {
            // Domain-specific exception -> GlobalExceptionHandler -> 404
            throw new MemberNotFoundException(id);
        }
        memberRepository.deleteById(id);
    }
}
