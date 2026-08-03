package br.com.dsuplementos.dto.pedido;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull(message = "Selecione o endereco de entrega.")
        Long enderecoId
) {
}
