package br.com.dsuplementos.dto.produto;

import br.com.dsuplementos.domain.enums.Categoria;

import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        String nome,
        Categoria categoria,
        Long marcaId,
        String marca,
        BigDecimal preco,
        int estoque,
        String descricao,
        String imagemUrl,
        boolean ativo,
        double mediaAvaliacoes,
        long quantidadeAvaliacoes
) {
}
