package br.com.dsuplementos.integration;

import br.com.dsuplementos.api.service.*;
import br.com.dsuplementos.domain.*;
import br.com.dsuplementos.domain.enums.*;
import br.com.dsuplementos.dto.pedido.CheckoutRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "app.jwt.secret=chave-exclusiva-de-testes-com-32-bytes-minimo",
        "app.seed-catalog.enabled=false", "app.admin.enabled=false", "spring.profiles.active=prod",
        "app.mail.enabled=true", "app.mail.from=loja@example.com", "app.mail.to=pedidos@example.com",
        "app.mail.api-key=chave-teste-sem-envio-real", "app.mail.poll-interval-ms=3600000",
        "app.messaging.orders.enabled=true"
})
@Testcontainers
class EmailPedidoIT {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @Container static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management");
    @DynamicPropertySource static void servicos(DynamicPropertyRegistry props) {
        props.add("spring.datasource.url", postgres::getJdbcUrl);
        props.add("spring.datasource.username", postgres::getUsername);
        props.add("spring.datasource.password", postgres::getPassword);
        props.add("spring.rabbitmq.addresses", rabbit::getAmqpUrl);
    }
    @Autowired PedidoService pedidos;
    @Autowired NotificacaoPedidoDispatcher dispatcher;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;
    @MockitoBean EmailPedidoGateway gateway;

    record Cliente(Usuario usuario, Long endereco) {}
    private Cliente cliente() {
        return tx.execute(status -> {
            String id = UUID.randomUUID().toString();
            Usuario usuario = new Usuario("Cliente", id + "@example.com", "hash", Role.CLIENTE); em.persist(usuario);
            Marca marca = new Marca(id); em.persist(marca);
            Produto produto = new Produto("Whey", Categoria.WHEY, marca, BigDecimal.TEN, 0, "Teste", null); em.persist(produto);
            Endereco endereco = new Endereco(usuario, "Casa", "01001-000", "Rua", "1", null, "Centro", "Sao Paulo", "SP", true); em.persist(endereco);
            Carrinho carrinho = new Carrinho(usuario);
            carrinho.adicionarItem(new ItemCarrinho(carrinho, produto, 1)); em.persist(carrinho); em.flush();
            return new Cliente(usuario, endereco.getId());
        });
    }
    @Test void pedidoGravaOutboxEConsomePeloRabbit() {
        Cliente c = cliente();
        Long pedido = pedidos.criarPedido(c.usuario(), new CheckoutRequest(c.endereco())).id();
        assertThat(jdbc.queryForObject("select count(*) from notificacoes_pedido where pedido_id = ?", Long.class, pedido)).isEqualTo(1);
        dispatcher.publicarPendentes();
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
            assertThat(jdbc.queryForObject("select enviado_em is not null from notificacoes_pedido where pedido_id = ?", Boolean.class, pedido)).isTrue());
        verify(gateway).enviar(eq(pedido), contains("Cliente"));
    }
    @Test void rollbackNaoDeixaNotificacaoOrfa() {
        Cliente c = cliente();
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            pedidos.criarPedido(c.usuario(), new CheckoutRequest(c.endereco()));
            throw new IllegalStateException("rollback solicitado no teste");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("select count(*) from pedidos where usuario_id = ?", Long.class, c.usuario().getId())).isZero();
        assertThat(jdbc.queryForObject("select count(*) from notificacoes_pedido n join pedidos p on p.id=n.pedido_id where p.usuario_id = ?", Long.class, c.usuario().getId())).isZero();
    }
    @Test void erroDoProvedorPodeSerReprocessadoSemPerderPedido() {
        Cliente c = cliente();
        Long pedido = pedidos.criarPedido(c.usuario(), new CheckoutRequest(c.endereco())).id();
        doThrow(new ResourceAccessException("falha simulada")).doNothing().when(gateway).enviar(eq(pedido), anyString());
        dispatcher.publicarPendentes();
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
            assertThat(jdbc.queryForObject("select tentativas from notificacoes_pedido where pedido_id = ?", Integer.class, pedido)).isEqualTo(1));
        jdbc.update("update notificacoes_pedido set proxima_tentativa=now(), publicado_em=null where pedido_id = ?", pedido);
        dispatcher.publicarPendentes();
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
            assertThat(jdbc.queryForObject("select enviado_em is not null from notificacoes_pedido where pedido_id = ?", Boolean.class, pedido)).isTrue());
        verify(gateway, times(2)).enviar(eq(pedido), anyString());
    }
}
