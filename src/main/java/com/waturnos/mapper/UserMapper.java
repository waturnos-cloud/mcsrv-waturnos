package com.waturnos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.waturnos.dto.beans.UserDTO;
import com.waturnos.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	
	@Mapping(target = "password", ignore = true)
	UserDTO toDto(User e);
	
	User toEntity(UserDTO d);

}
