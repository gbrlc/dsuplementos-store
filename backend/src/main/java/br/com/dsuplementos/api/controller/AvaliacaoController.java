package br.com.dsuplementos.api.controller;

import br.com.dsuplementos.api.service.AvaliacaoService;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.dto.avaliacao.AvaliacaoRequest;
import br.com.dsuplementos.dto.avaliacao.AvaliacaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping("/api/produtos/{produtoId}/avaliacoes")
    public List<AvaliacaoResponse> listar(@PathVariable Long produtoId) {
        return avaliacaoService.listarPorProduto(produtoId);
    }

    @PostMapping("/api/produtos/{produtoId}/avaliacoes")
    public ResponseEntity<AvaliacaoResponse> criar(
            @PathVariable Long produtoId,
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AvaliacaoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.criar(produtoId, usuario, request));
    }

    @PostMapping("/api/avaliacoes/{avaliacaoId}/fotos")
    public AvaliacaoResponse adicionarFotos(
            @PathVariable Long avaliacaoId,
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam("fotos") List<MultipartFile> fotos
    ) {
        return avaliacaoService.adicionarFotos(avaliacaoId, usuario, fotos);
    }
}
