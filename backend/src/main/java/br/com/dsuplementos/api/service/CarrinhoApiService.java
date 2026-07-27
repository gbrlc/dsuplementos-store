package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Carrinho;
import br.com.dsuplementos.domain.ItemCarrinho;
import br.com.dsuplementos.domain.Produto;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.dto.carrinho.CarrinhoResponse;
import br.com.dsuplementos.dto.carrinho.ItemCarrinhoResponse;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.repository.CarrinhoRepository;
import br.com.dsuplementos.repository.ItemCarrinhoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarrinhoApiService {

    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ProdutoService produtoService;

    public CarrinhoApiService(
            CarrinhoRepository carrinhoRepository,
            ItemCarrinhoRepository itemCarrinhoRepository,
            ProdutoService produtoService
    ) {
        this.carrinhoRepository = carrinhoRepository;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.produtoService = produtoService;
    }

    @Transactional
    public CarrinhoResponse listar(Usuario usuario) {
        return paraResponse(obterOuCriar(usuario));
    }

    @Transactional
    public CarrinhoResponse adicionar(Usuario usuario, Long produtoId, int quantidade) {
        Carrinho carrinho = obterOuCriar(usuario);
        Produto produto = produtoService.buscarEntidadeAtiva(produtoId);
        validarEstoque(produto, quantidade);

        ItemCarrinho item = carrinho.getItens().stream()
                .filter(itemCarrinho -> itemCarrinho.getProduto().getId().equals(produtoId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            ItemCarrinho novoItem = new ItemCarrinho(carrinho, produto, quantidade);
            carrinho.adicionarItem(novoItem);
            itemCarrinhoRepository.save(novoItem);
        } else {
            validarEstoque(produto, item.getQuantidade() + quantidade);
            item.setQuantidade(item.getQuantidade() + quantidade);
        }

        return paraResponse(carrinhoRepository.save(carrinho));
    }

    @Transactional
    public CarrinhoResponse atualizarQuantidade(Usuario usuario, Long produtoId, int quantidade) {
        Carrinho carrinho = obterOuCriar(usuario);
        ItemCarrinho item = encontrarItem(carrinho, produtoId);
        validarEstoque(item.getProduto(), quantidade);
        item.setQuantidade(quantidade);
        return paraResponse(carrinhoRepository.save(carrinho));
    }

    @Transactional
    public CarrinhoResponse remover(Usuario usuario, Long produtoId) {
        Carrinho carrinho = obterOuCriar(usuario);
        carrinho.removerItem(encontrarItem(carrinho, produtoId));
        return paraResponse(carrinhoRepository.save(carrinho));
    }

    private Carrinho obterOuCriar(Usuario usuario) {
        return carrinhoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> carrinhoRepository.save(new Carrinho(usuario)));
    }

    private ItemCarrinho encontrarItem(Carrinho carrinho, Long produtoId) {
        return carrinho.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Produto nao esta no carrinho."));
    }

    private void validarEstoque(Produto produto, int quantidade) {
        if (quantidade > produto.getEstoque()) {
            throw new RegraDeNegocioException("Quantidade maior que o estoque disponivel.");
        }
    }

    private CarrinhoResponse paraResponse(Carrinho carrinho) {
        List<ItemCarrinhoResponse> itens = carrinho.getItens().stream()
                .map(item -> {
                    Produto produto = item.getProduto();
                    BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
                    return new ItemCarrinhoResponse(
                            produto.getId(),
                            produto.getNome(),
                            produto.getImagemUrl(),
                            produto.getPreco(),
                            item.getQuantidade(),
                            subtotal
                    );
                })
                .toList();

        BigDecimal total = itens.stream()
                .map(ItemCarrinhoResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarrinhoResponse(itens, total);
    }
}
