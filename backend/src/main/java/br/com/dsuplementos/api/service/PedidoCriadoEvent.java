package br.com.dsuplementos.api.service;

// Dados pessoais ficam no banco, nao na mensagem do broker.
public record PedidoCriadoEvent(Long pedidoId) {}
