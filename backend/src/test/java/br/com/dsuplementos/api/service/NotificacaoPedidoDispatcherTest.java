package br.com.dsuplementos.api.service;

import br.com.dsuplementos.repository.NotificacaoPedidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.List;
import static org.mockito.Mockito.*;

class NotificacaoPedidoDispatcherTest {
    @Test void brokerForaDoArNaoRemoveNotificacaoPendente() {
        var repository = mock(NotificacaoPedidoRepository.class);
        var rabbit = mock(RabbitTemplate.class);
        when(repository.pendentesParaPublicar()).thenReturn(List.of(1L));
        doThrow(new AmqpConnectException(new java.net.ConnectException("teste")))
                .when(rabbit).convertAndSend(eq("pedidos"), eq("criado"), any(PedidoCriadoEvent.class));
        new NotificacaoPedidoDispatcher(repository, rabbit, "pedidos", "criado").publicarPendentes();
        verify(repository).publicado(1L);
        verify(repository, never()).enviado(anyLong());
        verify(repository, never()).falhou(anyLong(), anyInt());
    }
    @Test void publicaIdentificadorSemDadosPessoais() {
        var repository = mock(NotificacaoPedidoRepository.class);
        var rabbit = mock(RabbitTemplate.class);
        when(repository.pendentesParaPublicar()).thenReturn(List.of(1L));
        new NotificacaoPedidoDispatcher(repository, rabbit, "pedidos", "criado").publicarPendentes();
        verify(rabbit).convertAndSend("pedidos", "criado", new PedidoCriadoEvent(1L));
    }
}
