package br.com.dsuplementos.dto.avaliacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
        @NotNull(message = "Informe uma nota.")
        @Min(value = 1, message = "A nota deve ser entre 1 e 5.")
        @Max(value = 5, message = "A nota deve ser entre 1 e 5.")
        Short nota,

        @NotBlank(message = "Escreva sua avaliacao.")
        @Size(max = 1000)
        String comentario
) {
}
