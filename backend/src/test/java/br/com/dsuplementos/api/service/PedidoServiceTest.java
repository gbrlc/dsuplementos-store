package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Carrinho;
import br.com.dsuplementos.domain.Endereco;
import br.com.dsuplementos.domain.ItemCarrinho;
import br.com.dsuplementos.domain.Pedido;
import br.com.dsuplementos.domain.Produto;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Categoria;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.domain.enums.StatusPedido;
import br.com.dsuplementos.dto.pedido.CheckoutRequest;
import br.com.dsuplementos.dto.pedido.PedidoResponse;
import br.com.dsuplementos.repository.CarrinhoRepository;
import br.com.dsuplementos.repository.EnderecoRepository;
import br.com.dsuplementos.repository.PedidoRepository;
import br.com.dsuplementos.repository.ProdutoJpaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PedidoServiceTest {

    private final PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private final CarrinhoRepository carrinhoRepository = mock(CarrinhoRepository.class);
    private final EnderecoRepository enderecoRepository = mock(EnderecoRepository.class);
    private final ProdutoJpaRepository produtoRepository = mock(ProdutoJpaRepository.class);
    private final PedidoService pedidoService = new PedidoService(
            pedidoRepository,
            carrinhoRepository,
            enderecoRepository,
            produtoRepository
    );

    @Test
    void deveCriarPedidoBaixarEstoqueELimparCarrinho() {
        Usuario usuario = new Usuario("Ana Cliente", "ana@example.com", "hash", Role.CLIENTE);
        Produto produto = new Produto(
                "Whey", Categoria.WHEY, null, new BigDecimal("100.00"), 10, "Proteina", null
        );
        org.springframework.test.util.ReflectionTestUtils.setField(produto, "id", 1L);
        Endereco endereco = new Endereco(
                usuario, "Casa", "01001-000", "Praca da Se", "100", null, "Se", "Sao Paulo", "SP", true
        );
        Carrinho carrinho = new Carrinho(usuario);
        carrinho.adicionarItem(new ItemCarrinho(carrinho, produto, 2));

        when(carrinhoRepository.findByUsuarioIdComBloqueio(null)).thenReturn(Optional.of(carrinho));
        when(enderecoRepository.findByIdAndUsuarioId(null, null)).thenReturn(Optional.of(endereco));
        when(produtoRepository.findByIdComBloqueio(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponse pedido = pedidoService.criarPedido(usuario, new CheckoutRequest(null));

        assertThat(pedido.status()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.subtotal()).isEqualByComparingTo("200.00");
        assertThat(pedido.frete()).isEqualByComparingTo("0.00");
        assertThat(pedido.total()).isEqualByComparingTo("200.00");
        assertThat(pedido.itens()).hasSize(1);
        assertThat(produto.getEstoque()).isEqualTo(8);
        assertThat(carrinho.getItens()).isEmpty();
    }
    @Test void deveRecusarCarrinhoVazio() {
        Usuario usuario = new Usuario("Ana", "ana@example.com", "hash", Role.CLIENTE);
        when(carrinhoRepository.findByUsuarioIdComBloqueio(null)).thenReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> pedidoService.criarPedido(usuario, new CheckoutRequest(1L)))
                .isInstanceOf(br.com.dsuplementos.exception.RegraDeNegocioException.class);
        org.mockito.Mockito.verifyNoInteractions(pedidoRepository, produtoRepository);
    }

    @Test void naoDeveCancelarPedidoDeOutroUsuario() {
        Usuario usuario = new Usuario("Ana", "ana@example.com", "hash", Role.CLIENTE);
        when(pedidoRepository.findByIdAndUsuarioId(99L, null)).thenReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> pedidoService.cancelar(usuario, 99L))
                .isInstanceOf(br.com.dsuplementos.exception.RecursoNaoEncontradoException.class);
        org.mockito.Mockito.verifyNoInteractions(produtoRepository);
    }

    @Test void estoqueInsuficienteNaoCriaPedido() {
        validarProdutoIndisponivel(true, 1);
    }
    @Test void produtoInativoNaoCriaPedido() {
        validarProdutoIndisponivel(false, 10);
    }
    private void validarProdutoIndisponivel(boolean ativo, int estoque) {
        Usuario usuario = new Usuario("Ana", "ana@example.com", "hash", Role.CLIENTE);
        Produto produto = new Produto("Whey", Categoria.WHEY, null, new BigDecimal("100.00"), estoque, "Teste", null);
        org.springframework.test.util.ReflectionTestUtils.setField(produto, "id", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(produto, "ativo", ativo);
        Endereco endereco = new Endereco(usuario, "Casa", "01001-000", "Rua", "1", null, "Centro", "Sao Paulo", "SP", true);
        Carrinho carrinho = new Carrinho(usuario);
        carrinho.adicionarItem(new ItemCarrinho(carrinho, produto, 2));
        when(carrinhoRepository.findByUsuarioIdComBloqueio(null)).thenReturn(Optional.of(carrinho));
        when(enderecoRepository.findByIdAndUsuarioId(1L, null)).thenReturn(Optional.of(endereco));
        when(produtoRepository.findByIdComBloqueio(1L)).thenReturn(Optional.of(produto));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> pedidoService.criarPedido(usuario, new CheckoutRequest(1L)))
                .isInstanceOf(br.com.dsuplementos.exception.RegraDeNegocioException.class);
        assertThat(produto.getEstoque()).isEqualTo(estoque);
        assertThat(carrinho.getItens()).hasSize(1);
        org.mockito.Mockito.verifyNoInteractions(pedidoRepository);
    }

}
