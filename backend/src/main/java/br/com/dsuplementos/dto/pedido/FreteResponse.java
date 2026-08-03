package br.com.dsuplementos.dto.pedido;

import java.math.BigDecimal;

public record FreteResponse(BigDecimal subtotal, BigDecimal frete, BigDecimal total) {
}
