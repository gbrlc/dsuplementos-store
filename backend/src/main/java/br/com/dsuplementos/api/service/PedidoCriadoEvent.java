package br.com.dsuplementos.api.service;

import java.math.BigDecimal;
import java.time.Instant;

public record PedidoCriadoEvent(Long pedidoId, BigDecimal total, Instant criadoEm) {
}
