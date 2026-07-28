package br.com.dsuplementos.dto.comum;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas,
        boolean primeira,
        boolean ultima
) {
    public static <T, R> PaginaResponse<R> de(Page<T> pagina, Function<T, R> conversor) {
        return new PaginaResponse<>(
                pagina.getContent().stream().map(conversor).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast()
        );
    }
}
