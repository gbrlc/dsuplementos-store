package br.com.dsuplementos.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarPerfilRequest(
        @NotBlank(message = "Informe seu nome.")
        @Size(max = 120, message = "O nome pode ter no maximo 120 caracteres.")
        String nome
) {
}
