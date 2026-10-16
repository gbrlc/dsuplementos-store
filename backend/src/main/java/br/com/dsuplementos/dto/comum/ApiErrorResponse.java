package br.com.dsuplementos.dto.comum;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String erro,
        String codigo,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {
}
