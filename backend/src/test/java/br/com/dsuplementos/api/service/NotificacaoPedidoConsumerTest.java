package br.com.dsuplementos.api.service;

import br.com.dsuplementos.repository.NotificacaoPedidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import java.time.Instant;
import java.util.Optional;
import static org.mockito.Mockito.*;

class NotificacaoPedidoConsumerTest {
    private final NotificacaoPedidoRepository repository = mock(NotificacaoPedidoRepository.class);
    private final EmailPedidoGateway gateway = mock(EmailPedidoGateway.class);
    private final NotificacaoPedidoConsumer consumer = new NotificacaoPedidoConsumer(repository, gateway);
    @Test void enviaEMarcaAceiteDoProvedor() {
        when(repository.bloquear(1L)).thenReturn(Optional.of(registro(0, null, Instant.EPOCH)));
        consumer.receber(new PedidoCriadoEvent(1L));
        verify(gateway).enviar(1L, "{}");
        verify(repository).enviado(1L);
    }
    @Test void falhaDoProvedorFicaRegistradaParaRetry() {
        when(repository.bloquear(1L)).thenReturn(Optional.of(registro(0, null, Instant.EPOCH)));
        doThrow(new ResourceAccessException("timeout")).when(gateway).enviar(1L, "{}");
        consumer.receber(new PedidoCriadoEvent(1L));
        verify(repository).falhou(1L, 1);
        verify(repository, never()).enviado(anyLong());
    }
    @Test void mensagemDuplicadaNaoReenviaEmailConfirmado() {
        when(repository.bloquear(1L)).thenReturn(Optional.of(registro(0, Instant.now(), Instant.EPOCH)));
        consumer.receber(new PedidoCriadoEvent(1L));
        verifyNoInteractions(gateway);
    }
    @Test void respeitaBackoff() {
        when(repository.bloquear(1L)).thenReturn(Optional.of(registro(1, null, Instant.now().plusSeconds(60))));
        consumer.receber(new PedidoCriadoEvent(1L));
        verifyNoInteractions(gateway);
    }
    @Test void paraAposOitoFalhas() {
        when(repository.bloquear(1L)).thenReturn(Optional.of(registro(8, null, Instant.EPOCH)));
        consumer.receber(new PedidoCriadoEvent(1L));
        verifyNoInteractions(gateway);
    }
    private NotificacaoPedidoRepository.Notificacao registro(int tentativas, Instant enviado, Instant proxima) {
        return new NotificacaoPedidoRepository.Notificacao("{}", tentativas, proxima, enviado);
    }
}
