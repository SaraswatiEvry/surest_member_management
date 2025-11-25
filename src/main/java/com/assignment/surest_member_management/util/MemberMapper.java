package com.assignment.surest_member_management.util;

import com.assignment.surest_member_management.dto.MemberDTO;
import com.assignment.surest_member_management.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    MemberMapper  INSTANCE = Mappers.getMapper(MemberMapper.class);
    MemberDTO toMemberDTO(Member member);
    Member toMemberEntity(MemberDTO memberDTO);
}
