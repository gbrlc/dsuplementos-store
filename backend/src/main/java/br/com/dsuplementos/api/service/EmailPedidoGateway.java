package br.com.dsuplementos.api.service;

public interface EmailPedidoGateway {
    void enviar(Long pedidoId, String payload);
}
