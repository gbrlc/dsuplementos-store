package br.com.dsuplementos.repository;

import br.com.dsuplementos.model.Produto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoRepository {
    private final List<Produto> produtos = new ArrayList<>();

    public Produto salvar(Produto produto) {
        produtos.add(produto);
        return produto;
    }

    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos);
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtos.stream()
                .filter(produto -> produto.getId().equals(id))
                .findFirst();
    }
}
