package br.com.dsuplementos.dto.auth;

import br.com.dsuplementos.domain.enums.Role;

public record UsuarioResponse(Long id, String nome, String email, Role role) {
}
