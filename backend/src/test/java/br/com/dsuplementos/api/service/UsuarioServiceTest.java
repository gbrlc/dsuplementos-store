package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.dto.endereco.EnderecoRequest;
import br.com.dsuplementos.dto.endereco.EnderecoResponse;
import br.com.dsuplementos.dto.usuario.AlterarSenhaRequest;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.repository.EnderecoRepository;
import br.com.dsuplementos.repository.UsuarioRepository;
import br.com.dsuplementos.security.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final EnderecoRepository enderecoRepository = mock(EnderecoRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final UsuarioService usuarioService = new UsuarioService(
            usuarioRepository,
            enderecoRepository,
            passwordEncoder,
            refreshTokenService
    );

    @Test
    void deveAtualizarASenhaQuandoASenhaAtualForValida() {
        Usuario usuario = usuario();
        when(usuarioRepository.findById(null)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-atual", "hash-antigo")).thenReturn(true);
        when(passwordEncoder.matches("senha-nova", "hash-antigo")).thenReturn(false);
        when(passwordEncoder.encode("senha-nova")).thenReturn("hash-novo");

        usuarioService.alterarSenha(usuario, new AlterarSenhaRequest("senha-atual", "senha-nova"));

        assertThat(usuario.getSenhaHash()).isEqualTo("hash-novo");
        verify(refreshTokenService).revogarTodosDoUsuario(null);
    }

    @Test
    void deveRecusarAlteracaoQuandoASenhaAtualForIncorreta() {
        Usuario usuario = usuario();
        when(usuarioRepository.findById(null)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha-errada", "hash-antigo")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.alterarSenha(
                usuario,
                new AlterarSenhaRequest("senha-errada", "senha-nova")
        ))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("A senha atual esta incorreta.");
        assertThat(usuario.getSenhaHash()).isEqualTo("hash-antigo");
    }

    @Test
    void deveCriarPrimeiroEnderecoComoPrincipalENormalizarCep() {
        Usuario usuario = usuario();
        when(usuarioRepository.findById(null)).thenReturn(Optional.of(usuario));
        when(enderecoRepository.countByUsuarioId(null)).thenReturn(0L);
        when(enderecoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(enderecoRepository.findByUsuarioIdOrderByPrincipalDescIdDesc(null)).thenReturn(java.util.List.of());

        EnderecoResponse endereco = usuarioService.criarEndereco(usuario, new EnderecoRequest(
                "Casa",
                "01001000",
                "Praca da Se",
                "100",
                null,
                "Se",
                "Sao Paulo",
                "sp",
                false
        ));

        assertThat(endereco.principal()).isTrue();
        assertThat(endereco.cep()).isEqualTo("01001-000");
        assertThat(endereco.estado()).isEqualTo("SP");
    }

    private Usuario usuario() {
        return new Usuario("Ana Cliente", "ana@example.com", "hash-antigo", Role.CLIENTE);
    }
}
