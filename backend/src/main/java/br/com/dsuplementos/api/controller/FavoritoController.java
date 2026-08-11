package br.com.dsuplementos.api.controller;

import br.com.dsuplementos.api.service.FavoritoService;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.dto.favorito.FavoritoStatusResponse;
import br.com.dsuplementos.dto.produto.ProdutoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @GetMapping
    public List<ProdutoResponse> listar(@AuthenticationPrincipal Usuario usuario) {
        return favoritoService.listar(usuario);
    }

    @GetMapping("/{produtoId}")
    public FavoritoStatusResponse status(@AuthenticationPrincipal Usuario usuario, @PathVariable Long produtoId) {
        return favoritoService.status(usuario, produtoId);
    }

    @PostMapping("/{produtoId}")
    public ResponseEntity<Void> adicionar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long produtoId) {
        favoritoService.adicionar(usuario, produtoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{produtoId}")
    public ResponseEntity<Void> remover(@AuthenticationPrincipal Usuario usuario, @PathVariable Long produtoId) {
        favoritoService.remover(usuario, produtoId);
        return ResponseEntity.noContent().build();
    }
}
