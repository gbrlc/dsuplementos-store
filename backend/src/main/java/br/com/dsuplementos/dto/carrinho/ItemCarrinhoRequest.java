package br.com.dsuplementos.dto.carrinho;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCarrinhoRequest(
        @NotNull(message = "Informe o produto.")
        Long produtoId,
        @NotNull(message = "Informe a quantidade.")
        @Min(value = 1, message = "A quantidade deve ser maior que zero.")
        Integer quantidade
) {
}
