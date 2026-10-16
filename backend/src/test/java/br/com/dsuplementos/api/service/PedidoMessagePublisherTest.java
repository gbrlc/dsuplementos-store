package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Pedido;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.mockito.Mockito.*;

class PedidoMessagePublisherTest {
    @Test void soEnviaAposCommit() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        Pedido pedido = mock(Pedido.class);
        PedidoMessagePublisher publisher = new PedidoMessagePublisher(rabbit, true, "pedidos", "criado");
        TransactionSynchronizationManager.initSynchronization();
        try {
            publisher.publicarCriacao(pedido);
            verifyNoInteractions(rabbit);
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
            verify(rabbit).convertAndSend(eq("pedidos"), eq("criado"), any(PedidoCriadoEvent.class));
        } finally { TransactionSynchronizationManager.clearSynchronization(); }
    }
    @Test void rollbackNaoEnvia() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        TransactionSynchronizationManager.initSynchronization();
        try {
            new PedidoMessagePublisher(rabbit, true, "pedidos", "criado").publicarCriacao(mock(Pedido.class));
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(1));
            verifyNoInteractions(rabbit);
        } finally { TransactionSynchronizationManager.clearSynchronization(); }
    }
}
