package br.com.dsuplementos.integration;

import br.com.dsuplementos.api.service.PedidoService;
import br.com.dsuplementos.domain.*;
import br.com.dsuplementos.domain.enums.*;
import br.com.dsuplementos.dto.pedido.CheckoutRequest;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
        "app.jwt.secret=chave-exclusiva-de-testes-com-32-bytes-minimo",
        "app.seed-catalog.enabled=false", "app.admin.enabled=false",
        "app.messaging.orders.enabled=false", "spring.profiles.active=prod"
})
@Testcontainers
class PedidoIT {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void banco(DynamicPropertyRegistry props) {
        props.add("spring.datasource.url", postgres::getJdbcUrl);
        props.add("spring.datasource.username", postgres::getUsername);
        props.add("spring.datasource.password", postgres::getPassword);
    }
    @Autowired PedidoService pedidos;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;

    record Cenario(Usuario usuario, Long produto, Long endereco) {}
    private Cenario preparar(int estoque, int quantidade) {
        return tx.execute(status -> {
            String id = UUID.randomUUID().toString();
            Usuario usuario = new Usuario("Cliente", id + "@example.com", "hash", Role.CLIENTE);
            em.persist(usuario);
            Marca marca = new Marca(id); em.persist(marca);
            Produto produto = new Produto("Whey", Categoria.WHEY, marca, new BigDecimal("100.00"), estoque, "Teste", null);
            em.persist(produto);
            Endereco endereco = new Endereco(usuario, "Casa", "01001-000", "Rua", "1", null, "Centro", "Sao Paulo", "SP", true);
            em.persist(endereco);
            Carrinho carrinho = new Carrinho(usuario);
            carrinho.adicionarItem(new ItemCarrinho(carrinho, produto, quantidade));
            em.persist(carrinho); em.flush();
            return new Cenario(usuario, produto.getId(), endereco.getId());
        });
    }
    private int estoque(Long id) {
        return tx.execute(status -> em.find(Produto.class, id).getEstoque());
    }
    @Test void solicitacaoECancelamentoNaoAlteramSaldoLegado() {
        Cenario c = preparar(3, 2);
        var pedido = pedidos.criarPedido(c.usuario(), new CheckoutRequest(c.endereco()));
        assertThat(estoque(c.produto())).isEqualTo(3);
        pedidos.cancelar(c.usuario(), pedido.id());
        assertThat(estoque(c.produto())).isEqualTo(3);
        assertThatThrownBy(() -> pedidos.cancelar(c.usuario(), pedido.id())).isInstanceOf(RegraDeNegocioException.class);
        assertThat(estoque(c.produto())).isEqualTo(3);
    }
    @Test void produtoInativoDesfazPedidoInteiro() {
        Cenario c = preparar(3, 2);
        tx.executeWithoutResult(status -> {
            Produto primeiro = em.find(Produto.class, c.produto());
            Produto indisponivel = new Produto("Indisponivel", Categoria.WHEY, primeiro.getMarca(), BigDecimal.TEN, 0, "Teste", null);
            org.springframework.test.util.ReflectionTestUtils.setField(indisponivel, "ativo", false);
            em.persist(indisponivel);
            Carrinho carrinho = em.createQuery("select c from Carrinho c where c.usuario.id = :id", Carrinho.class)
                    .setParameter("id", c.usuario().getId()).getSingleResult();
            carrinho.adicionarItem(new ItemCarrinho(carrinho, indisponivel, 1));
        });
        assertThatThrownBy(() -> pedidos.criarPedido(c.usuario(), new CheckoutRequest(c.endereco())))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThat(estoque(c.produto())).isEqualTo(3);
        tx.executeWithoutResult(status -> {
            assertThat(em.createQuery("select count(p) from Pedido p where p.usuario.id = :id", Long.class)
                    .setParameter("id", c.usuario().getId()).getSingleResult()).isZero();
            assertThat(em.createQuery("select count(i) from ItemCarrinho i where i.carrinho.usuario.id = :id", Long.class)
                    .setParameter("id", c.usuario().getId()).getSingleResult()).isEqualTo(2);
        });
    }
    @Test void cancelamentosConcorrentesRecompoemUmaVez() throws Exception {
        Cenario c = preparar(3, 2);
        var pedido = pedidos.criarPedido(c.usuario(), new CheckoutRequest(c.endereco()));
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Callable<Boolean> cancelar = () -> {
            inicio.await();
            try { pedidos.cancelar(c.usuario(), pedido.id()); return true; }
            catch (RegraDeNegocioException e) { return false; }
        };
        try {
            Future<Boolean> a = pool.submit(cancelar), b = pool.submit(cancelar);
            inicio.countDown();
            assertThat(java.util.List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            assertThat(estoque(c.produto())).isEqualTo(3);
        } finally { pool.shutdownNow(); }
    }
    @Test void solicitacoesConcorrentesNaoDependemDoSaldoLegado() throws Exception {
        Cenario a = preparar(1, 1);
        Cenario b = tx.execute(status -> {
            Usuario usuario = new Usuario("Outro", UUID.randomUUID() + "@example.com", "hash", Role.CLIENTE);
            em.persist(usuario);
            Endereco endereco = new Endereco(usuario, "Casa", "01001-000", "Rua", "1", null, "Centro", "Sao Paulo", "SP", true);
            em.persist(endereco);
            Carrinho carrinho = new Carrinho(usuario);
            carrinho.adicionarItem(new ItemCarrinho(carrinho, em.find(Produto.class, a.produto()), 1));
            em.persist(carrinho); em.flush();
            return new Cenario(usuario, a.produto(), endereco.getId());
        });
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            java.util.function.Function<Cenario, Callable<Boolean>> comprar = c -> () -> {
                inicio.await();
                try { pedidos.criarPedido(c.usuario(), new CheckoutRequest(c.endereco())); return true; }
                catch (RegraDeNegocioException e) { return false; }
            };
            Future<Boolean> primeiro = pool.submit(comprar.apply(a)), segundo = pool.submit(comprar.apply(b));
            inicio.countDown();
            assertThat(java.util.List.of(primeiro.get(20, TimeUnit.SECONDS), segundo.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, true);
            assertThat(estoque(a.produto())).isEqualTo(1);
        } finally { pool.shutdownNow(); }
    }

    @Autowired br.com.dsuplementos.security.RefreshTokenService refresh;
    @Test void mesmoRefreshTokenNaoPodeSerRenovadoDuasVezesConcorrentemente() throws Exception {
        Cenario c = preparar(1, 1);
        String token = refresh.emitir(c.usuario());
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Callable<Boolean> renovar = () -> {
            inicio.await();
            try { refresh.renovar(token); return true; }
            catch (RegraDeNegocioException e) { return false; }
        };
        try {
            Future<Boolean> a = pool.submit(renovar), b = pool.submit(renovar);
            inicio.countDown();
            assertThat(java.util.List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        } finally { pool.shutdownNow(); }
    }

}
