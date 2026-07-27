package br.com.dsuplementos.dto.carrinho;

import java.math.BigDecimal;
import java.util.List;

public record CarrinhoResponse(List<ItemCarrinhoResponse> itens, BigDecimal total) {
}
