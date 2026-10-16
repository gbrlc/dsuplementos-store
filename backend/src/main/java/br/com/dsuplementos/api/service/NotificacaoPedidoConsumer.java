package br.com.dsuplementos.api.service;

import br.com.dsuplementos.repository.NotificacaoPedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import java.time.Instant;

@Service
@ConditionalOnProperty(name = {"app.mail.enabled", "app.messaging.orders.enabled"}, havingValue = "true")
public class NotificacaoPedidoConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificacaoPedidoConsumer.class);
    private final NotificacaoPedidoRepository notificacoes;
    private final EmailPedidoGateway gateway;
    public NotificacaoPedidoConsumer(NotificacaoPedidoRepository notificacoes, EmailPedidoGateway gateway) {
        this.notificacoes = notificacoes; this.gateway = gateway;
    }
    @RabbitListener(queues = "${app.messaging.orders.queue}")
    @Transactional
    public void receber(PedidoCriadoEvent evento) {
        var registro = notificacoes.bloquear(evento.pedidoId()).orElse(null);
        if (registro == null || !registro.podeEnviar(Instant.now())) { return; }
        try {
            gateway.enviar(evento.pedidoId(), registro.payload());
            notificacoes.enviado(evento.pedidoId());
        } catch (RestClientException exception) {
            int tentativa = registro.tentativas() + 1;
            notificacoes.falhou(evento.pedidoId(), tentativa);
            // Nao registrar corpo HTTP, chaves ou dados pessoais do pedido.
            log.warn("Email do pedido {} falhou na tentativa {} de 8", evento.pedidoId(), tentativa);
        }
    }
}
