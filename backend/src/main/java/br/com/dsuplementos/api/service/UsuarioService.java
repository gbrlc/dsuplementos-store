package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Endereco;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.dto.auth.UsuarioResponse;
import br.com.dsuplementos.dto.endereco.EnderecoRequest;
import br.com.dsuplementos.dto.endereco.EnderecoResponse;
import br.com.dsuplementos.dto.usuario.AlterarSenhaRequest;
import br.com.dsuplementos.dto.usuario.AtualizarPerfilRequest;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.exception.RecursoNaoEncontradoException;
import br.com.dsuplementos.repository.EnderecoRepository;
import br.com.dsuplementos.repository.UsuarioRepository;
import br.com.dsuplementos.security.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            EnderecoRepository enderecoRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(Usuario usuarioAutenticado) {
        return paraUsuarioResponse(buscarUsuario(usuarioAutenticado.getId()));
    }

    @Transactional
    public UsuarioResponse atualizarPerfil(Usuario usuarioAutenticado, AtualizarPerfilRequest request) {
        Usuario usuario = buscarUsuario(usuarioAutenticado.getId());
        usuario.setNome(request.nome().trim());
        return paraUsuarioResponse(usuario);
    }

    @Transactional
    public void alterarSenha(Usuario usuarioAutenticado, AlterarSenhaRequest request) {
        Usuario usuario = buscarUsuario(usuarioAutenticado.getId());
        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("A senha atual esta incorreta.");
        }
        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenhaHash())) {
            throw new RegraDeNegocioException("A nova senha deve ser diferente da senha atual.");
        }
        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        refreshTokenService.revogarTodosDoUsuario(usuario.getId());
    }

    @Transactional(readOnly = true)
    public List<EnderecoResponse> listarEnderecos(Usuario usuarioAutenticado) {
        return enderecoRepository.findByUsuarioIdOrderByPrincipalDescIdDesc(usuarioAutenticado.getId())
                .stream()
                .map(this::paraEnderecoResponse)
                .toList();
    }

    @Transactional
    public EnderecoResponse criarEndereco(Usuario usuarioAutenticado, EnderecoRequest request) {
        Usuario usuario = buscarUsuario(usuarioAutenticado.getId());
        boolean principal = Boolean.TRUE.equals(request.principal())
                || enderecoRepository.countByUsuarioId(usuario.getId()) == 0;

        Endereco endereco = new Endereco(
                usuario,
                limpar(request.apelido()),
                normalizarCep(request.cep()),
                request.logradouro().trim(),
                request.numero().trim(),
                limpar(request.complemento()),
                request.bairro().trim(),
                request.cidade().trim(),
                request.estado().trim().toUpperCase(Locale.ROOT),
                principal
        );
        enderecoRepository.save(endereco);
        if (principal) {
            definirEnderecoPrincipal(usuario.getId(), endereco);
        }
        return paraEnderecoResponse(endereco);
    }

    @Transactional
    public EnderecoResponse atualizarEndereco(Usuario usuarioAutenticado, Long enderecoId, EnderecoRequest request) {
        Endereco endereco = buscarEnderecoDoUsuario(enderecoId, usuarioAutenticado.getId());
        endereco.atualizar(
                limpar(request.apelido()),
                normalizarCep(request.cep()),
                request.logradouro().trim(),
                request.numero().trim(),
                limpar(request.complemento()),
                request.bairro().trim(),
                request.cidade().trim(),
                request.estado().trim().toUpperCase(Locale.ROOT)
        );
        if (Boolean.TRUE.equals(request.principal())) {
            definirEnderecoPrincipal(usuarioAutenticado.getId(), endereco);
        }
        return paraEnderecoResponse(endereco);
    }

    @Transactional
    public void removerEndereco(Usuario usuarioAutenticado, Long enderecoId) {
        Endereco endereco = buscarEnderecoDoUsuario(enderecoId, usuarioAutenticado.getId());
        long quantidadeEnderecos = enderecoRepository.countByUsuarioId(usuarioAutenticado.getId());
        if (endereco.isPrincipal() && quantidadeEnderecos > 1) {
            throw new RegraDeNegocioException("Defina outro endereco como principal antes de remover este.");
        }
        enderecoRepository.delete(endereco);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado."));
    }

    private Endereco buscarEnderecoDoUsuario(Long enderecoId, Long usuarioId) {
        return enderecoRepository.findByIdAndUsuarioId(enderecoId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereco nao encontrado."));
    }

    private void definirEnderecoPrincipal(Long usuarioId, Endereco enderecoPrincipal) {
        enderecoRepository.findByUsuarioIdOrderByPrincipalDescIdDesc(usuarioId)
                .forEach(endereco -> endereco.setPrincipal(endereco.getId().equals(enderecoPrincipal.getId())));
    }

    private UsuarioResponse paraUsuarioResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole());
    }

    private EnderecoResponse paraEnderecoResponse(Endereco endereco) {
        return new EnderecoResponse(
                endereco.getId(),
                endereco.getApelido(),
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.isPrincipal()
        );
    }

    private String normalizarCep(String cep) {
        String apenasNumeros = cep.replaceAll("\\D", "");
        return apenasNumeros.substring(0, 5) + "-" + apenasNumeros.substring(5);
    }

    private String limpar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
