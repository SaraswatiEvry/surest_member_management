package com.surest.util;

import com.surest.dto.MemberDTO;
import com.surest.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    MemberMapper  INSTANCE = Mappers.getMapper(MemberMapper.class);
    MemberDTO toMemberDTO(Member member);


    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Member toMemberEntity(MemberDTO memberDTO);
}
