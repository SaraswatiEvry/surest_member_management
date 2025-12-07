package com.surest.service;

import com.surest.dto.MemberDTO;
import com.surest.entity.Member;
import com.surest.exception.MemberAlreadyExistsException;
import com.surest.exception.MemberNotFoundException;
import com.surest.repository.MemberRepository;
import com.surest.util.MemberMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Override
    @Transactional
    public Page<MemberDTO> getAllMembers(int page, int size, String sort, String firstName,
                                         String lastName) {

        log.info("Fetching all members: page={}, size={}, sort={}, firstName={}, lastName={}",
                page, size, sort, firstName, lastName);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.by(sort.split(",")[0])
                .with(Sort.Direction.fromString(sort.split(",")[1]))));

        Specification<Member> spec = Specification.unrestricted();

        if (firstName != null && !firstName.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        }
        if (lastName != null && !lastName.isEmpty()) {
            spec = spec.and((root, query, cb)
                    -> cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
        }
        return memberRepository.findAll(spec, pageable).map(memberMapper::toMemberDTO);
    }

    @Override
    @Cacheable(value = "members", key = "#id.toString()")
    @Transactional
    public MemberDTO getMemberById(UUID id) {
        log.info("Fetching member by ID: {}", id);
        return memberRepository.findById(id)
                .map(memberMapper::toMemberDTO)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Override
    @Transactional
    public MemberDTO createMember(MemberDTO dto) {
        log.info("Creating new member: {} {}", dto.getFirstName(), dto.getLastName());
        if (memberRepository.existsByEmail((dto.getEmail()))) {
            throw new MemberAlreadyExistsException(dto.getEmail());
        }
        Member member = memberMapper.toMemberEntity(dto);
        return memberMapper.toMemberDTO(memberRepository.save(member));
    }

    @Transactional
    @Override
    @CacheEvict(value = "members", key = "#id.toString()")
    public MemberDTO updateMember(UUID id, MemberDTO dto) {
        log.info("Updating member with ID: {}", id);
        Member existing = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        existing.setDateOfBirth(dto.getDateOfBirth());

        return memberMapper.toMemberDTO(memberRepository.save(existing));
    }

    @Transactional
    @Override
    @CacheEvict(value = "members", key = "#id.toString()")
    public void deleteMember(UUID id) {
        log.info("Deleting member with ID: {}", id);
        if (!memberRepository.existsById(id)) {
            throw new RuntimeException("Member not found");
        }
        memberRepository.deleteById(id);
    }
}
