package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.dto.auth.*;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.repository.UsuarioRepository;
import br.com.dsuplementos.security.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtService jwt = mock(JwtService.class);
    private final RefreshTokenService refresh = mock(RefreshTokenService.class);
    private final AuthService auth = new AuthService(usuarios, encoder, jwt, refresh);

    @Test void senhaIncorretaNaoEmiteSessao() {
        Usuario usuario = new Usuario("Ana", "ana@example.com", "hash", Role.CLIENTE);
        when(usuarios.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("errada", "hash")).thenReturn(false);
        assertThatThrownBy(() -> auth.login(new LoginRequest("ANA@example.com", "errada")))
                .isInstanceOf(RegraDeNegocioException.class).hasMessage("E-mail ou senha invalidos.");
        verifyNoInteractions(jwt, refresh);
    }
    @Test void renovacaoEmiteTokensSubstitutos() {
        Usuario usuario = new Usuario("Ana", "ana@example.com", "hash", Role.CLIENTE);
        when(refresh.renovar("anterior")).thenReturn(usuario);
        when(refresh.emitir(usuario)).thenReturn("novo-refresh");
        when(jwt.gerarToken(usuario)).thenReturn("novo-jwt");
        AuthResponse resposta = auth.renovar(new RefreshTokenRequest("anterior"));
        assertThat(resposta.token()).isEqualTo("novo-jwt");
        assertThat(resposta.refreshToken()).isEqualTo("novo-refresh");
        var ordem = inOrder(refresh);
        ordem.verify(refresh).renovar("anterior");
        ordem.verify(refresh).emitir(usuario);
    }
}
