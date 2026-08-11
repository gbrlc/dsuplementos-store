package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Favorito;
import br.com.dsuplementos.domain.Produto;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Categoria;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.repository.FavoritoRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FavoritoServiceTest {

    private final FavoritoRepository favoritoRepository = mock(FavoritoRepository.class);
    private final ProdutoService produtoService = mock(ProdutoService.class);
    private final FavoritoService favoritoService = new FavoritoService(favoritoRepository, produtoService);

    @Test
    void deveSalvarFavoritoQuandoProdutoAindaNaoFoiSalvoPeloUsuario() {
        Usuario usuario = usuario();
        Produto produto = produto();
        when(favoritoRepository.existsByUsuarioIdAndProdutoId(null, null)).thenReturn(false);
        when(produtoService.buscarEntidadeAtiva(null)).thenReturn(produto);

        favoritoService.adicionar(usuario, null);

        verify(favoritoRepository).save(any(Favorito.class));
    }

    @Test
    void naoDeveDuplicarUmFavoritoExistente() {
        when(favoritoRepository.existsByUsuarioIdAndProdutoId(null, null)).thenReturn(true);

        favoritoService.adicionar(usuario(), null);

        verify(favoritoRepository, never()).save(any());
        verify(produtoService, never()).buscarEntidadeAtiva(any());
    }

    @Test
    void deveRemoverFavoritoEncontradoParaAConta() {
        Favorito favorito = new Favorito(usuario(), produto());
        when(favoritoRepository.findByUsuarioIdAndProdutoId(null, null)).thenReturn(Optional.of(favorito));

        favoritoService.remover(usuario(), null);

        verify(favoritoRepository).delete(favorito);
    }

    private Usuario usuario() {
        return new Usuario("Ana Cliente", "ana@example.com", "hash", Role.CLIENTE);
    }

    private Produto produto() {
        return new Produto("Whey", Categoria.WHEY, null, BigDecimal.TEN, 10, "Proteina", null);
    }
}
