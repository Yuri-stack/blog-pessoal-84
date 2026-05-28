package com.generation.blogpessoal.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;

/*	Classe de Serviço que busca os dados de um usuário
 * 	que está fazendo o Login/Autenticação e transforma 
 * 	em um objeto da Classe UserDetails, ou seja, no 
 * 	formato que a Spring Security entende. 
 * */

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired	// Inversão/Injeção de Dependência
	private UsuarioRepository usuarioRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		if (username == null || username.trim().isEmpty()) {
			throw new UsernameNotFoundException("Usuário (e-mail) não pode ser vazio");
		}
		
		Optional<Usuario> usuario = usuarioRepository.findByUsuario(username);

		if (usuario.isPresent()) {
			return new UserDetailsImpl(usuario.get());
		}else {
			throw new UsernameNotFoundException("Usuário não encontrado: " + username);
		}
			
	}
}