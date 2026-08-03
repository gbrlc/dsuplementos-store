package br.com.dsuplementos.security;

import br.com.dsuplementos.domain.RefreshToken;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final RefreshTokenRepository refreshTokenRepository;
    private final long expiracaoDias;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration-days}") long expiracaoDias
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expiracaoDias = expiracaoDias;
    }

    @Transactional
    public String emitir(Usuario usuario) {
        String tokenPuro = gerarTokenPuro();
        RefreshToken token = new RefreshToken(
                usuario,
                calcularHash(tokenPuro),
                Instant.now().plus(expiracaoDias, ChronoUnit.DAYS)
        );
        refreshTokenRepository.save(token);
        return tokenPuro;
    }

    @Transactional
    public Usuario renovar(String tokenPuro) {
        RefreshToken token = buscarValido(tokenPuro);
        token.revogar();
        return token.getUsuario();
    }

    @Transactional
    public void revogar(String tokenPuro) {
        if (tokenPuro == null || tokenPuro.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(calcularHash(tokenPuro))
                .ifPresent(RefreshToken::revogar);
    }

    private RefreshToken buscarValido(String tokenPuro) {
        if (tokenPuro == null || tokenPuro.isBlank()) {
            throw sessaoInvalida();
        }

        RefreshToken token = refreshTokenRepository.findByTokenHash(calcularHash(tokenPuro))
                .orElseThrow(this::sessaoInvalida);
        if (!token.estaValido(Instant.now()) || !token.getUsuario().isAtivo()) {
            throw sessaoInvalida();
        }
        return token;
    }

    private RegraDeNegocioException sessaoInvalida() {
        return new RegraDeNegocioException("Sessao invalida ou expirada. Faca login novamente.");
    }

    private String gerarTokenPuro() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String calcularHash(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 nao esta disponivel na JVM.", exception);
        }
    }
}
