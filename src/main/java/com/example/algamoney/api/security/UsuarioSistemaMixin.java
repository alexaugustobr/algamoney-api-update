package com.example.algamoney.api.security;

import com.example.algamoney.api.model.Usuario;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioSistemaMixin {

	@JsonCreator
	public UsuarioSistemaMixin(@JsonProperty("principal") String username,
	                           @JsonProperty("password") String password,
	                           @JsonProperty("username") Collection<? extends GrantedAuthority> authorities,
	                           @JsonProperty("usuario") Usuario usuario) {
		
	}
	
}
