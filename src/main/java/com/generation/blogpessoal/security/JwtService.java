package com.generation.blogpessoal.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

    private String secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    private Duration expiration = Duration.ofMinutes(60);
    
    // Definindo uma variavel do tipo SecretKey
    private SecretKey signingKey;
        
    // Método para criar chave de assinatura para o Token
    private SecretKey getSigningKey() {
        if (signingKey == null) {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return signingKey;
    }
    
    // Método que extrai as informações ocultas no Token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    // Método que extrai o usuário(e-mail) do Token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Método que extrai as informações referente a expiração do Token
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // Método que valida o Token
    public boolean validateToken(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject().equals(userDetails.getUsername()) && 
               claims.getExpiration().after(new Date());
    }

    // Método que gera o Token, colocando o e-mail, tempo de expiração e chave de assinatura
    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expiration)))
            .signWith(getSigningKey())
            .compact();
    }
    
}