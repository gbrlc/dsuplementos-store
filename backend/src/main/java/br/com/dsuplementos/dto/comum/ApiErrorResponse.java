package br.com.dsuplementos.dto.comum;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
public class ApiErrorResponse {
    Instant timestamp;
    int status;
    String erro;
    String codigo;
    String mensagem;
    String caminho;
    Map<String, String> campos;
}
