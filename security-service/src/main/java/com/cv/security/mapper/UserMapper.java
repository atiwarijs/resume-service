package com.cv.security.mapper;

import com.cv.security.entity.User;
import com.cv.security.dto.UserDTO;

public class UserMapper {

	public static UserDTO toDto(User user) {
		UserDTO userDTO = new UserDTO();
		userDTO.setId(user.getId());
		userDTO.setUsername(user.getUsername());
		userDTO.setEmail(user.getEmail());
		userDTO.setFirstName(user.getFirstName());
		userDTO.setLastName(user.getLastName());
		userDTO.setCreatedAt(user.getCreatedAt());
		userDTO.setUpdatedAt(user.getUpdatedAt());
		userDTO.setCreatedBy(user.getCreatedBy());
		userDTO.setUpdatedBy(user.getUpdatedBy());
		return userDTO;
	}
}
