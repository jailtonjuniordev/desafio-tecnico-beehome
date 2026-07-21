package com.jjdev.beehome_tecnico.auth.domain.model;

import com.jjdev.beehome_tecnico.shared.domain.model.BaseModel;

import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel extends BaseModel {

	private String username;
	private String email;
	private String password;

	public static UserModel create(String username, String email, String hashedPassword) {
		return UserModel.builder()
				.username(username)
				.email(email)
				.password(hashedPassword)
				.build();
	}

	public UserModel update(String username, String email, String hashedPassword) {
		return UserModel.builder()
				.id(getId())
				.username(StringUtils.isNotBlank(username) ? username : getUsername())
				.email(StringUtils.isNotBlank(email) ? email : getEmail())
				.password(StringUtils.isNotBlank(hashedPassword) ? hashedPassword : getPassword())
				.createdAt(getCreatedAt())
				.updatedAt(getUpdatedAt())
				.build();
	}
}
