package br.com.dsuplementos.dto.pedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        Long produtoId,
        String nome,
        BigDecimal precoUnitario,
        int quantidade,
        BigDecimal subtotal
) {
}
