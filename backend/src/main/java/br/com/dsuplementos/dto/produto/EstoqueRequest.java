package br.com.dsuplementos.dto.produto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EstoqueRequest(
        @NotNull(message = "Informe o estoque.")
        @Min(value = 0, message = "O estoque nao pode ser negativo.")
        Integer estoque
) {
}
