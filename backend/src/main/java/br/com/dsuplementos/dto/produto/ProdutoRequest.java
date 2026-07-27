package br.com.dsuplementos.dto.produto;

import br.com.dsuplementos.domain.enums.Categoria;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "Informe o nome do produto.")
        @Size(max = 150)
        String nome,

        @NotNull(message = "Informe uma categoria.")
        Categoria categoria,

        @NotNull(message = "Informe uma marca.")
        Long marcaId,

        @NotNull(message = "Informe o preco.")
        @DecimalMin(value = "0.00", message = "O preco nao pode ser negativo.")
        BigDecimal preco,

        @NotNull(message = "Informe o estoque.")
        @Min(value = 0, message = "O estoque nao pode ser negativo.")
        Integer estoque,

        @NotBlank(message = "Informe a descricao.")
        @Size(max = 4000)
        String descricao,

        @Size(max = 500)
        String imagemUrl,

        Boolean ativo
) {
}
