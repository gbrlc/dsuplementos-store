package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Pedido;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PedidoMessagePublisher {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PedidoMessagePublisher.class);
    private final RabbitTemplate rabbitTemplate;
    private final boolean enabled;
    private final String exchange;
    private final String routingKey;

    public PedidoMessagePublisher(RabbitTemplate rabbitTemplate,
                                  @Value("${app.messaging.orders.enabled:false}") boolean enabled,
                                  @Value("${app.messaging.orders.exchange}") String exchange,
                                  @Value("${app.messaging.orders.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.enabled = enabled;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publicarCriacao(Pedido pedido) {
        if (enabled) {
            PedidoCriadoEvent evento = new PedidoCriadoEvent(pedido.getId(), pedido.getTotal(), pedido.getCriadoEm());
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                rabbitTemplate.convertAndSend(exchange, routingKey, evento);
                            } catch (org.springframework.amqp.AmqpException exception) {
                                log.error("Pedido {} confirmado, mas evento nao enviado ao RabbitMQ", pedido.getId(), exception);
                            }
                        }
                    });
        }
    }
}
