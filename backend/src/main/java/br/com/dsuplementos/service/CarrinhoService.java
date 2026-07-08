package br.com.dsuplementos.service;

import br.com.dsuplementos.model.Produto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class CarrinhoService {
    private final ProdutoService produtoService;
    private final Map<Long, Integer> itens = new LinkedHashMap<>();

    public CarrinhoService(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    public void adicionarProduto(Long produtoId, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade precisa ser maior que zero.");
        }

        Produto produto = produtoService.buscarPorId(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado."));

        if (quantidade > produto.getEstoque()) {
            throw new IllegalArgumentException("Quantidade maior que o estoque disponivel.");
        }

        itens.merge(produtoId, quantidade, Integer::sum);
    }

    public Map<Produto, Integer> listarItens() {
        Map<Produto, Integer> produtosComQuantidade = new LinkedHashMap<>();

        itens.forEach((produtoId, quantidade) -> {
            Optional<Produto> produto = produtoService.buscarPorId(produtoId);
            produto.ifPresent(valor -> produtosComQuantidade.put(valor, quantidade));
        });

        return produtosComQuantidade;
    }

    public BigDecimal calcularTotal() {
        return listarItens().entrySet().stream()
                .map(item -> item.getKey().getPreco().multiply(BigDecimal.valueOf(item.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void limpar() {
        itens.clear();
    }
}
