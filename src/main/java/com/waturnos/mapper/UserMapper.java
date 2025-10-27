package com.waturnos.mapper;

import org.mapstruct.Mapper;

import com.waturnos.dto.beans.UserDTO;
import com.waturnos.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	
	
	UserDTO toDto(User e);
	
	User toEntity(UserDTO d);

}
