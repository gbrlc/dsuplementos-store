package br.com.dsuplementos.api.service;

import br.com.dsuplementos.repository.NotificacaoPedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = {"app.mail.enabled", "app.messaging.orders.enabled"}, havingValue = "true")
public class NotificacaoPedidoDispatcher {
    private static final Logger log = LoggerFactory.getLogger(NotificacaoPedidoDispatcher.class);
    private final NotificacaoPedidoRepository notificacoes;
    private final RabbitTemplate rabbit;
    private final String exchange;
    private final String routingKey;
    public NotificacaoPedidoDispatcher(NotificacaoPedidoRepository notificacoes, RabbitTemplate rabbit,
            @Value("${app.messaging.orders.exchange}") String exchange,
            @Value("${app.messaging.orders.routing-key}") String routingKey) {
        this.notificacoes = notificacoes; this.rabbit = rabbit;
        this.exchange = exchange; this.routingKey = routingKey;
    }
    @Scheduled(fixedDelayString = "${app.mail.poll-interval-ms:30000}", initialDelayString = "${app.mail.poll-interval-ms:30000}")
    @Transactional
    public void publicarPendentes() {
        for (Long pedidoId : notificacoes.pendentesParaPublicar()) {
            notificacoes.publicado(pedidoId);
            try {
                rabbit.convertAndSend(exchange, routingKey, new PedidoCriadoEvent(pedidoId));
            } catch (AmqpException exception) {
                // A linha continua pendente e sera publicada novamente apos dois minutos.
                log.warn("Broker indisponivel para notificacao do pedido {}", pedidoId);
            }
        }
    }
}
