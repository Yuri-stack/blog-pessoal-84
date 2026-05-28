package com.generation.blogpessoal.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/* Essa Classe funciona como um Filtro, agora todas as requições
 * que forem feitas, serão passadas por essa classe para verificarmos
 * e validarmos o Token JWT*/

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired	// Inversão/Injeção de Dependência
    private JwtService jwtService;

    @Autowired	// Inversão/Injeção de Dependência
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            processJwtAuthentication(request, token);
            filterChain.doFilter(request, response);
            
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException
        		| UsernameNotFoundException e) {
        	response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    // Método que extrai o Token do Cabeçalho Authorization da nossa Requisição
    private String extractTokenFromRequest(HttpServletRequest request) {
        
    	String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
            return authHeader.substring(7);
        }
        
        return null;
    }
    
    /* Método que utiliza os métodos da Classe de Serviço jwtService
     * para assegurar que o Token que foi recebido é um token válido
     * para ser usado na Autorização */
    private void processJwtAuthentication(HttpServletRequest request, String token) {
        
    	String username = jwtService.extractUsername(token);
        
        if (username != null && !username.trim().isEmpty()) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtService.validateToken(token, userDetails)) {
            	
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
            } else {
                throw new RuntimeException("Token JWT inválido ou expirado");
            }
            
        } else {
            throw new RuntimeException("Usuário não pode ser extraído do token JWT");
        }
    }

}