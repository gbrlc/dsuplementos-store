package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.dto.auth.AuthResponse;
import br.com.dsuplementos.dto.auth.CadastroRequest;
import br.com.dsuplementos.dto.auth.LoginRequest;
import br.com.dsuplementos.dto.auth.UsuarioResponse;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.exception.RecursoNaoEncontradoException;
import br.com.dsuplementos.repository.UsuarioRepository;
import br.com.dsuplementos.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
        return autenticar(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(request.email()))
                .orElseThrow(() -> new RegraDeNegocioException("E-mail ou senha invalidos."));

        if (!usuario.isAtivo() || !passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("E-mail ou senha invalidos.");
        }

        return autenticar(usuario);
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

    private AuthResponse autenticar(Usuario usuario) {
        return new AuthResponse(jwtService.gerarToken(usuario), paraResponse(usuario));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
