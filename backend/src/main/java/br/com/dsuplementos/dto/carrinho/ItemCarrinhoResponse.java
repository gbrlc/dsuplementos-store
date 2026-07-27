package br.com.dsuplementos.dto.carrinho;

import java.math.BigDecimal;

public record ItemCarrinhoResponse(
        Long produtoId,
        String nome,
        String imagemUrl,
        BigDecimal precoUnitario,
        int quantidade,
        BigDecimal subtotal
) {
}
