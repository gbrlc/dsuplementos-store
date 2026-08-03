package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.dto.auth.AuthResponse;
import br.com.dsuplementos.dto.auth.CadastroRequest;
import br.com.dsuplementos.dto.auth.LoginRequest;
import br.com.dsuplementos.dto.auth.RefreshTokenRequest;
import br.com.dsuplementos.dto.auth.UsuarioResponse;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.exception.RecursoNaoEncontradoException;
import br.com.dsuplementos.repository.UsuarioRepository;
import br.com.dsuplementos.security.JwtService;
import br.com.dsuplementos.security.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse cadastrar(CadastroRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraDeNegocioException("Ja existe uma conta com este e-mail.");
        }

        Usuario usuario = new Usuario(
                request.nome().trim(),
                email,
                passwordEncoder.encode(request.senha()),
                Role.CLIENTE
        );
        usuarioRepository.save(usuario);
        return criarSessao(usuario);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(request.email()))
                .orElseThrow(() -> new RegraDeNegocioException("E-mail ou senha invalidos."));

        if (!usuario.isAtivo() || !passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("E-mail ou senha invalidos.");
        }

        return criarSessao(usuario);
    }

    @Transactional
    public AuthResponse renovar(RefreshTokenRequest request) {
        Usuario usuario = refreshTokenService.renovar(request.refreshToken());
        return criarSessao(usuario);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revogar(request.refreshToken());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
        return paraResponse(usuario);
    }

    public static UsuarioResponse paraResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole());
    }

    private AuthResponse criarSessao(Usuario usuario) {
        return new AuthResponse(
                jwtService.gerarToken(usuario),
                refreshTokenService.emitir(usuario),
                paraResponse(usuario)
        );
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
