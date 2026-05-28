package com.generation.blogpessoal.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.generation.blogpessoal.model.Usuario;

/*	Classe que indica para o Spring como é formato do 
 * 	objeto usuario no momento do Login.
 * 	 
 * 	No caso, ela indica que o nosso usuário possui um
 * 	username e uma senha. A partir disso, a Sprin Security
 * 	consegue saber como irá executar a validação de 
 * 	Autenticação/Login e Autorização.
 * */

public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 1L;

	private String username;
	private String password;

	public UserDetailsImpl(Usuario user) {
		this.username = user.getUsuario();
		this.password = user.getSenha();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList();  
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {

		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}