package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Pedido;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.repository.NotificacaoPedidoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class PedidoMessagePublisher {
    private final NotificacaoPedidoRepository notificacoes;
    private final ObjectMapper mapper;
    private final String remetente;
    private final String destinatario;

    public PedidoMessagePublisher(NotificacaoPedidoRepository notificacoes, ObjectMapper mapper,
            @Value("${app.mail.from:}") String remetente, @Value("${app.mail.to:}") String destinatario,
            @Value("${app.mail.api-key:}") String apiKey,
            @Value("${app.messaging.orders.enabled:false}") boolean messagingEnabled) {
        if (remetente.isBlank() || destinatario.isBlank() || apiKey.isBlank() || !messagingEnabled) {
            throw new IllegalStateException("Envio de email exige APP_MAIL_FROM, APP_MAIL_TO, RESEND_API_KEY e APP_MESSAGING_ORDERS_ENABLED=true.");
        }
        this.notificacoes = notificacoes;
        this.mapper = mapper;
        this.remetente = remetente;
        this.destinatario = destinatario;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publicarCriacao(Pedido pedido, Usuario usuario) {
        StringBuilder texto = new StringBuilder("Nova solicitacao #").append(pedido.getId())
                .append("\nCliente: ").append(usuario.getNome()).append("\nEmail: ").append(usuario.getEmail())
                .append("\n\nItens:\n");
        pedido.getItens().forEach(item -> texto.append(item.getQuantidade()).append(" x ")
                .append(item.getNomeProduto()).append(" - R$ ").append(item.getPrecoUnitario()).append("\n"));
        texto.append("\nSubtotal: R$ ").append(pedido.getSubtotal())
                .append("\nFrete simulado: R$ ").append(pedido.getFrete())
                .append("\nTotal estimado: R$ ").append(pedido.getTotal())
                .append("\n\nSolicitacao de interesse. Confirmar disponibilidade, entrega e valores no atendimento pelo WhatsApp. Nao representa pagamento ou venda concluida.");
        try {
            String payload = mapper.writeValueAsString(Map.of("from", remetente, "to", List.of(destinatario),
                    "reply_to", usuario.getEmail(), "subject", "DSuplementos - solicitacao #" + pedido.getId(),
                    "text", texto.toString()));
            notificacoes.registrar(pedido.getId(), payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Nao foi possivel preparar notificacao do pedido.", exception);
        }
    }
}
