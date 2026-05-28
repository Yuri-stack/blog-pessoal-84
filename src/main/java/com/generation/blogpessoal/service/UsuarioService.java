package com.generation.blogpessoal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.model.UsuarioLogin;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.security.JwtService;

@Service
public class UsuarioService {

	@Autowired	// Inversão/Injeção de Dependência
	private UsuarioRepository usuarioRepository;

	@Autowired	// Inversão/Injeção de Dependência
	private JwtService jwtService;

	@Autowired	// Inversão/Injeção de Dependência
	private AuthenticationManager authenticationManager;

	@Autowired	// Inversão/Injeção de Dependência
	private PasswordEncoder passwordEncoder;

	public List<Usuario> getAll() {
		return usuarioRepository.findAll();
	}

	public Optional<Usuario> getById(Long id) {
		return usuarioRepository.findById(id);
	}

	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {

		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent()) {
			return Optional.empty();
		}

		/*	O passwordEncoder é um objeto que tem o método ENCODE que permite 
		 * 	encriptografar a senha conforme a configuração feita na Security Config. */
		usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
		usuario.setId(null);
		
		return Optional.of(usuarioRepository.save(usuario));
	}

	public Optional<Usuario> atualizarUsuario(Usuario usuario) {

		if (!usuarioRepository.findById(usuario.getId()).isPresent()) {
			return Optional.empty();
		}

		Optional<Usuario> usuarioExistente = usuarioRepository.findByUsuario(usuario.getUsuario());
		
		/*	Nesse IF estavamos verificando se o Optional usuarioExistente possui um objeto dentro dele
		 * 	e se os dados que você está tentando gravar já não pertencem a outro registro */
		if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(usuario.getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe!", null);
		}

		// Mesmo comportamento do comentário da linha 48
		usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
		
		// Atualiza o registro no BD e retorna um Optional a Controller
		return Optional.of(usuarioRepository.save(usuario));	 
	}
	
	public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin) {

		if (!usuarioLogin.isPresent()) {
			return Optional.empty();
		}

		/*	O login aqui é um objeto que foi encontrado dentro do Optional */
		UsuarioLogin login = usuarioLogin.get();
		
		try {
			
			/*	O authenticationManager é um objeto que tem o método AUTHENTICATE 
			 * 	que permite VALIDAR e AUTENTICAr um usuário (email, senha) conforme 
			 * 	a configuração feita na Security Config. */
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(login.getUsuario(), login.getSenha()));

			return usuarioRepository.findByUsuario(login.getUsuario())
				.map(usuario -> construirRespostaLogin(login, usuario));

		} catch (Exception e) {

			return Optional.empty();
		}
	}

	private UsuarioLogin construirRespostaLogin(UsuarioLogin usuarioLogin, Usuario usuario) {
		
		/*	Como o nome do método diz, aqui recebemos os dados do banco através do 
		 * 	argumento USUARIO, mas precisamos enviar para o Cliente, um objeto da
		 * 	classe USUARIOLOGIN, devido a questão do Token.
		 * 
		 * 	Em outras palavras, entre um Objeto Usuario {id, nome, foto, email, senha, postagens}
		 * 	e sai um objeto UsuarioLogin { id, nome, foto, token } 
		 * */
		
		usuarioLogin.setId(usuario.getId());
		usuarioLogin.setNome(usuario.getNome());
		usuarioLogin.setFoto(usuario.getFoto());
		usuarioLogin.setSenha("");
		usuarioLogin.setToken(gerarToken(usuario.getUsuario()));	
		return usuarioLogin;
		
	}

	private String gerarToken(String usuario) {
		/* Utilizamos o método generateToken da classe de Serviço 
		 * para montar o Token a partir do email do usuario. */ 
		
		return "Bearer " + jwtService.generateToken(usuario);
	}
}