package br.com.dsuplementos.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.Duration;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class ResendEmailPedidoGateway implements EmailPedidoGateway {
    private final RestClient client;
    public ResendEmailPedidoGateway(@Value("${app.mail.api-key:}") String apiKey,
            @Value("${app.mail.base-url:https://api.resend.com}") String baseUrl) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + apiKey).build();
    }
    @Override
    public void enviar(Long pedidoId, String payload) {
        client.post().uri("/emails").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "dsuplementos/pedido/" + pedidoId)
                .body(payload).retrieve().toBodilessEntity();
    }
}
