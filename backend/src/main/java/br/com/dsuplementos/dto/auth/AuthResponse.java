package br.com.dsuplementos.dto.auth;

public record AuthResponse(String token, String refreshToken, UsuarioResponse usuario) {
}
