package br.com.dsuplementos.service;

import br.com.dsuplementos.model.Categoria;
import br.com.dsuplementos.model.Produto;
import br.com.dsuplementos.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProdutoService {
    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto cadastrar(Produto produto) {
        validarProduto(produto);
        return produtoRepository.salvar(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.listarTodos();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.buscarPorId(id);
    }

    public List<Produto> buscarPorCategoria(Categoria categoria) {
        return produtoRepository.listarTodos().stream()
                .filter(produto -> produto.getCategoria() == categoria)
                .toList();
    }

    public List<Produto> buscarPorFaixaDePreco(BigDecimal precoMinimo, BigDecimal precoMaximo) {
        return produtoRepository.listarTodos().stream()
                .filter(produto -> produto.getPreco().compareTo(precoMinimo) >= 0)
                .filter(produto -> produto.getPreco().compareTo(precoMaximo) <= 0)
                .toList();
    }

    public List<Produto> ordenarPorMenorPreco() {
        return produtoRepository.listarTodos().stream()
                .sorted(Comparator.comparing(Produto::getPreco))
                .toList();
    }

    public Set<Categoria> listarCategoriasCadastradas() {
        return produtoRepository.listarTodos().stream()
                .map(Produto::getCategoria)
                .collect(Collectors.toSet());
    }

    private void validarProduto(Produto produto) {
        if (produto.getId() == null) {
            throw new IllegalArgumentException("Produto precisa ter ID.");
        }

        if (produto.getNome() == null || produto.getNome().isBlank()) {
            throw new IllegalArgumentException("Produto precisa ter nome.");
        }

        if (produto.getPreco() == null || produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Produto precisa ter preco maior que zero.");
        }

        if (produto.getEstoque() < 0) {
            throw new IllegalArgumentException("Estoque nao pode ser negativo.");
        }
    }
}
