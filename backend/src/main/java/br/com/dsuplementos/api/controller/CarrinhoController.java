package br.com.dsuplementos.api.controller;

import br.com.dsuplementos.api.service.CarrinhoApiService;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.dto.carrinho.CarrinhoResponse;
import br.com.dsuplementos.dto.carrinho.ItemCarrinhoRequest;
import br.com.dsuplementos.dto.carrinho.QuantidadeRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrinho")
public class CarrinhoController {

    private final CarrinhoApiService carrinhoService;

    public CarrinhoController(CarrinhoApiService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @GetMapping
    public CarrinhoResponse listar(@AuthenticationPrincipal Usuario usuario) {
        return carrinhoService.listar(usuario);
    }

    @PostMapping("/itens")
    public CarrinhoResponse adicionar(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody ItemCarrinhoRequest request
    ) {
        return carrinhoService.adicionar(usuario, request.produtoId(), request.quantidade());
    }

    @PatchMapping("/itens/{produtoId}")
    public CarrinhoResponse atualizarQuantidade(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long produtoId,
            @Valid @RequestBody QuantidadeRequest request
    ) {
        return carrinhoService.atualizarQuantidade(usuario, produtoId, request.quantidade());
    }

    @DeleteMapping("/itens/{produtoId}")
    public CarrinhoResponse remover(@AuthenticationPrincipal Usuario usuario, @PathVariable Long produtoId) {
        return carrinhoService.remover(usuario, produtoId);
    }
}
