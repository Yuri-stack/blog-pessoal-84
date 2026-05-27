package com.generation.blogpessoal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.TemaRepository;

import jakarta.validation.Valid;

//Anotações: alterar e/ou definir comportamentos

@RestController					// Indica que a classe é uma Controller (Recebe Requisições e Responde)
@RequestMapping("/postagens")	// Indica que as requisições do endpoint "/postagens" serão tratadas por essa controller 
@CrossOrigin(origins = "*", allowedHeaders = "*")	// Permite que essa controller receba requisições de qualquer Front 
public class PostagemController {
	
	@Autowired	// Inversão de Dependência/Controle
	private PostagemRepository postagemRepository;
	
	@Autowired	// Injeção de Dependência
	private TemaRepository temaRepository;
	
	// /postagens
	@GetMapping	// Todas as requisições do tipo GET vão ser executadas por esse método
	public ResponseEntity<List<Postagem>> buscarTodasAsPostagens(){		
		return ResponseEntity.ok(postagemRepository.findAll());
	}
	
	@GetMapping("/{id}")	// /postagens/123
	public ResponseEntity<Postagem> buscarPostagemPeloId(@PathVariable Long id){
		
		return postagemRepository.findById(id)	// métodos de manipulação de dados em SQL
				
				// findById == Optional[ Postagem ]
				
				.map(postagemEncontrada -> ResponseEntity.ok(postagemEncontrada))	// resp => objeto dentro do Optional
				
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
		// 		.orElse(ResponseEntity.notFound().build());
	}
	
	@GetMapping("/titulo/{titulo}")	// /postagens/titulo/algum_texto
	public ResponseEntity<List<Postagem>> getByTitulo(@PathVariable String titulo){
		return ResponseEntity.ok(
				postagemRepository.findAllByTituloContainingIgnoreCase(titulo));
	}

	@PostMapping	// /postagens && Verbo HTTP for POST
	public ResponseEntity<Postagem> cadastrar(@Valid @RequestBody Postagem post) {
		
		// post.getTema().getId()) == pegue o id do tema que está dentro da Postagem
		if(temaRepository.existsById(post.getTema().getId())) {	
			post.setId(null);
			
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(postagemRepository.save(post));
		}
		
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tema não existe!", null);
	}
	
	@PutMapping
	public ResponseEntity<Postagem> atualizar(@Valid @RequestBody Postagem postagem) {
		
		if (postagemRepository.existsById(postagem.getId())) {

			if (temaRepository.existsById(postagem.getTema().getId()))
				return ResponseEntity.status(HttpStatus.OK).body(postagemRepository.save(postagem));

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tema não existe!", null);

		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}
	
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}")	// postagens/1 && Verbo HTTP == Delete
	public void delete(@PathVariable Long id) {
		
		Optional<Postagem> optinal_postagem = postagemRepository.findById(id);
		
		if(optinal_postagem.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		
		postagemRepository.deleteById(id);				
	}	

}
