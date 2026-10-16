package br.com.dsuplementos.api.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientResponseException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;

class ResendEmailPedidoGatewayTest {
    @Test void enviaPayloadEChaveEstavelPorHttp() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> chave = new AtomicReference<>(), corpo = new AtomicReference<>();
        server.createContext("/emails", exchange -> {
            chave.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            corpo.set(new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(200, -1); exchange.close();
        });
        server.start();
        try {
            var gateway = new ResendEmailPedidoGateway("chave-teste", "http://127.0.0.1:" + server.getAddress().getPort());
            gateway.enviar(42L, "{\"text\":\"Solicitacao\"}");
            assertThat(chave.get()).isEqualTo("dsuplementos/pedido/42");
            assertThat(corpo.get()).contains("Solicitacao");
        } finally { server.stop(0); }
    }
    @Test void erroHttpNaoETratadoComoSucesso() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/emails", exchange -> { exchange.sendResponseHeaders(429, -1); exchange.close(); });
        server.start();
        try {
            var gateway = new ResendEmailPedidoGateway("chave-teste", "http://127.0.0.1:" + server.getAddress().getPort());
            assertThatThrownBy(() -> gateway.enviar(1L, "{}" )).isInstanceOf(RestClientResponseException.class);
        } finally { server.stop(0); }
    }
}
