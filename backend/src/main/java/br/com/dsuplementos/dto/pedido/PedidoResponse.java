package br.com.dsuplementos.dto.pedido;

import br.com.dsuplementos.domain.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(
        Long id,
        StatusPedido status,
        BigDecimal subtotal,
        BigDecimal desconto,
        BigDecimal frete,
        BigDecimal total,
        EnderecoPedidoResponse endereco,
        List<ItemPedidoResponse> itens,
        Instant criadoEm
) {
}
