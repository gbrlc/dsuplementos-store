package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.*;
import br.com.dsuplementos.domain.enums.*;
import br.com.dsuplementos.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarrinhoApiServiceTest {
    @Test void saldoLegadoZeroNaoBloqueiaSolicitacao() {
        var carrinhos = mock(CarrinhoRepository.class);
        var itens = mock(ItemCarrinhoRepository.class);
        var produtos = mock(ProdutoService.class);
        var usuario = new Usuario("Ana", "ana@example.com", "hash", Role.CLIENTE);
        var produto = new Produto("Whey", Categoria.WHEY, null, BigDecimal.TEN, 0, "Teste", null);
        var carrinho = new Carrinho(usuario);
        when(carrinhos.findByUsuarioIdComBloqueio(null)).thenReturn(Optional.of(carrinho));
        when(produtos.buscarEntidadeAtiva(1L)).thenReturn(produto);
        when(carrinhos.save(carrinho)).thenReturn(carrinho);
        var resposta = new CarrinhoApiService(carrinhos, itens, produtos).adicionar(usuario, 1L, 2);
        assertThat(resposta.itens()).hasSize(1);
        assertThat(resposta.itens().get(0).quantidade()).isEqualTo(2);
        assertThat(produto.getEstoque()).isZero();
    }
}
