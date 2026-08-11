package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Favorito;
import br.com.dsuplementos.domain.Produto;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.dto.favorito.FavoritoStatusResponse;
import br.com.dsuplementos.dto.produto.ProdutoResponse;
import br.com.dsuplementos.repository.FavoritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ProdutoService produtoService;

    public FavoritoService(FavoritoRepository favoritoRepository, ProdutoService produtoService) {
        this.favoritoRepository = favoritoRepository;
        this.produtoService = produtoService;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(Usuario usuario) {
        return favoritoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId())
                .stream()
                .map(Favorito::getProduto)
                .filter(Produto::isAtivo)
                .map(produtoService::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FavoritoStatusResponse status(Usuario usuario, Long produtoId) {
        return new FavoritoStatusResponse(favoritoRepository.existsByUsuarioIdAndProdutoId(usuario.getId(), produtoId));
    }

    @Transactional
    public void adicionar(Usuario usuario, Long produtoId) {
        if (favoritoRepository.existsByUsuarioIdAndProdutoId(usuario.getId(), produtoId)) {
            return;
        }
        Produto produto = produtoService.buscarEntidadeAtiva(produtoId);
        favoritoRepository.save(new Favorito(usuario, produto));
    }

    @Transactional
    public void remover(Usuario usuario, Long produtoId) {
        favoritoRepository.findByUsuarioIdAndProdutoId(usuario.getId(), produtoId)
                .ifPresent(favoritoRepository::delete);
    }
}
