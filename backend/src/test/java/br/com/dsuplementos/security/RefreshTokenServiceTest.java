package br.com.dsuplementos.security;

import br.com.dsuplementos.domain.RefreshToken;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final RefreshTokenService refreshTokenService = new RefreshTokenService(refreshTokenRepository, 14);

    @Test
    void deveRotacionarUmRefreshTokenValido() {
        Usuario usuario = new Usuario("Ana Cliente", "ana@example.com", "hash", Role.CLIENTE);
        RefreshToken token = new RefreshToken(usuario, "hash-do-token", Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        Usuario resultado = refreshTokenService.renovar("refresh-token-original");

        assertThat(resultado).isSameAs(usuario);
        assertThat(token.estaValido(Instant.now())).isFalse();
    }

    @Test
    void deveRejeitarUmRefreshTokenDesconhecido() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.renovar("token-invalido"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Sessao invalida ou expirada. Faca login novamente.");
    }
    @Test void deveRejeitarTokenExpirado() {
        Usuario usuario = new Usuario("Ana", "ana@example.com", "hash", Role.CLIENTE);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(
                new RefreshToken(usuario, "hash", Instant.now().minusSeconds(1))));
        assertThatThrownBy(() -> refreshTokenService.renovar("expirado"))
                .isInstanceOf(RegraDeNegocioException.class);
    }
    @Test void deveRejeitarTokenRevogado() {
        Usuario usuario = new Usuario("Ana", "ana@example.com", "hash", Role.CLIENTE);
        RefreshToken token = new RefreshToken(usuario, "hash", Instant.now().plusSeconds(60));
        token.revogar();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        assertThatThrownBy(() -> refreshTokenService.renovar("revogado"))
                .isInstanceOf(RegraDeNegocioException.class);
    }

}
