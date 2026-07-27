package br.com.dsuplementos.dto.avaliacao;

import java.time.Instant;
import java.util.List;

public record AvaliacaoResponse(
        Long id,
        String usuario,
        short nota,
        String comentario,
        Instant criadoEm,
        List<FotoAvaliacaoResponse> fotos
) {
}
