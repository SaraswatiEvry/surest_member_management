package com.assignment.surest_member_management.util;

import com.assignment.surest_member_management.dto.MemberDTO;
import com.assignment.surest_member_management.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public static MemberDTO toMemberDTO(Member member) {
        return new MemberDTO(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getDateOfBirth(),
                member.getEmail()
        );
    }

    public Member toMemberEntity(MemberDTO memberDTO) {
        Member member = new Member();
        member.setId(memberDTO.getId());
        member.setFirstName(memberDTO.getFirstName());
        member.setLastName(memberDTO.getLastName());
        member.setDateOfBirth(memberDTO.getDateOfBirth());
        member.setEmail(memberDTO.getEmail());
        return member;
    }
}
