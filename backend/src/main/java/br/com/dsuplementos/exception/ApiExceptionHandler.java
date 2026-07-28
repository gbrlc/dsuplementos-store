package br.com.dsuplementos.exception;

import br.com.dsuplementos.dto.comum.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> tratarNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.NOT_FOUND, "RECURSO_NAO_ENCONTRADO", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ApiErrorResponse> tratarRegraDeNegocio(
            RegraDeNegocioException exception,
            HttpServletRequest request
    ) {
        return resposta(HttpStatus.BAD_REQUEST, "REGRA_DE_NEGOCIO", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : exception.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }

        return resposta(HttpStatus.BAD_REQUEST, "VALIDACAO", "Existem campos invalidos.", request, campos);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> tratarRequisicaoInvalida(Exception exception, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Nao foi possivel interpretar a requisicao.", request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> resposta(
            HttpStatus status,
            String codigo,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> campos
    ) {
        ApiErrorResponse corpo = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .erro(status.getReasonPhrase())
                .codigo(codigo)
                .mensagem(mensagem)
                .caminho(request.getRequestURI())
                .campos(campos)
                .build();
        return ResponseEntity.status(status).body(corpo);
    }
}
