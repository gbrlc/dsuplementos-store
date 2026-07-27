package br.com.dsuplementos.dto.auth;

public record AuthResponse(String token, UsuarioResponse usuario) {
}
