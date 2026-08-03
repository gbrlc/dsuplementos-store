package br.com.dsuplementos.api.controller;

import br.com.dsuplementos.api.service.UsuarioService;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.dto.auth.UsuarioResponse;
import br.com.dsuplementos.dto.endereco.EnderecoRequest;
import br.com.dsuplementos.dto.endereco.EnderecoResponse;
import br.com.dsuplementos.dto.usuario.AlterarSenhaRequest;
import br.com.dsuplementos.dto.usuario.AtualizarPerfilRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/me")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public UsuarioResponse perfil(@AuthenticationPrincipal Usuario usuario) {
        return usuarioService.perfil(usuario);
    }

    @PutMapping
    public UsuarioResponse atualizarPerfil(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AtualizarPerfilRequest request
    ) {
        return usuarioService.atualizarPerfil(usuario, request);
    }

    @PatchMapping("/senha")
    public ResponseEntity<Void> alterarSenha(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AlterarSenhaRequest request
    ) {
        usuarioService.alterarSenha(usuario, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/enderecos")
    public List<EnderecoResponse> listarEnderecos(@AuthenticationPrincipal Usuario usuario) {
        return usuarioService.listarEnderecos(usuario);
    }

    @PostMapping("/enderecos")
    public ResponseEntity<EnderecoResponse> criarEndereco(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody EnderecoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarEndereco(usuario, request));
    }

    @PutMapping("/enderecos/{enderecoId}")
    public EnderecoResponse atualizarEndereco(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long enderecoId,
            @Valid @RequestBody EnderecoRequest request
    ) {
        return usuarioService.atualizarEndereco(usuario, enderecoId, request);
    }

    @DeleteMapping("/enderecos/{enderecoId}")
    public ResponseEntity<Void> removerEndereco(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long enderecoId
    ) {
        usuarioService.removerEndereco(usuario, enderecoId);
        return ResponseEntity.noContent().build();
    }
}
