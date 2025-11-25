package com.assignment.surest_member_management.service;

import com.assignment.surest_member_management.dto.MemberDTO;
import com.assignment.surest_member_management.entity.Member;
import com.assignment.surest_member_management.repository.MemberRepository;
import com.assignment.surest_member_management.util.MemberMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Override
    public Page<MemberDTO> getAllMembers(int page, int size, String sort, String firstName,
                                         String lastName) {

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
    public MemberDTO getMemberById(UUID id) {
        System.out.println("Fetching data for Id : " + id);
        return memberRepository.findById(id)
                .map(memberMapper::toMemberDTO)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    }

    @Override
    public MemberDTO createMember(MemberDTO dto) {
        if (memberRepository.existsByEmail((dto.getEmail()))) {
            throw new EntityExistsException("Member already exists");
        }
        Member member = memberMapper.toMemberEntity(dto);
        return memberMapper.toMemberDTO(memberRepository.save(member));
    }

    @Transactional
    @Override
    @CacheEvict(value = "members", key = "#id.toString()")
    public MemberDTO updateMember(UUID id, MemberDTO dto) {
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
        if (!memberRepository.existsById(id)) {
            throw new RuntimeException("Member not found");
        }
        memberRepository.deleteById(id);
    }
}
