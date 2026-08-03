package br.com.dsuplementos.api.controller;

import br.com.dsuplementos.api.service.PedidoService;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.dto.pedido.CheckoutRequest;
import br.com.dsuplementos.dto.pedido.FreteResponse;
import br.com.dsuplementos.dto.pedido.PedidoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/simulacao-frete")
    public FreteResponse simularFrete(@AuthenticationPrincipal Usuario usuario) {
        return pedidoService.simularFrete(usuario);
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody CheckoutRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criarPedido(usuario, request));
    }

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal Usuario usuario) {
        return pedidoService.listar(usuario);
    }

    @PostMapping("/{pedidoId}/pagamento-simulado")
    public PedidoResponse simularPagamento(@AuthenticationPrincipal Usuario usuario, @PathVariable Long pedidoId) {
        return pedidoService.simularPagamento(usuario, pedidoId);
    }

    @PostMapping("/{pedidoId}/cancelamento")
    public PedidoResponse cancelar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long pedidoId) {
        return pedidoService.cancelar(usuario, pedidoId);
    }
}
