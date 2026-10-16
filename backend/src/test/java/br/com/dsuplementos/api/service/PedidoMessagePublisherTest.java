package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.*;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.repository.NotificacaoPedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoMessagePublisherTest {
    @Test void preparaSnapshotDoEmailSemEnviarNaTransacaoDoPedido() throws Exception {
        var repository = mock(NotificacaoPedidoRepository.class);
        var mapper = new ObjectMapper();
        var publisher = new PedidoMessagePublisher(repository, mapper, "loja@example.com", "pedidos@example.com", "teste", true);
        var usuario = new Usuario("Ana", "ana@example.com", "hash", Role.CLIENTE);
        var endereco = new Endereco(usuario, "Casa", "01001-000", "Rua", "1", null, "Centro", "Sao Paulo", "SP", true);
        var pedido = new Pedido(usuario, endereco, BigDecimal.TEN, BigDecimal.ZERO);
        org.springframework.test.util.ReflectionTestUtils.setField(pedido, "id", 1L);
        publisher.publicarCriacao(pedido, usuario);
        var payload = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(repository).registrar(eq(1L), payload.capture());
        var json = mapper.readTree(payload.getValue());
        assertThat(json.get("reply_to").asText()).isEqualTo("ana@example.com");
        assertThat(json.get("to").get(0).asText()).isEqualTo("pedidos@example.com");
        assertThat(json.get("text").asText()).contains("Ana", "WhatsApp", "Nao representa pagamento");
    }
    @Test void recusaAtivacaoSemConfiguracaoCompleta() {
        assertThatThrownBy(() -> new PedidoMessagePublisher(mock(NotificacaoPedidoRepository.class), new ObjectMapper(), "", "", "", false))
                .isInstanceOf(IllegalStateException.class);
    }
}
