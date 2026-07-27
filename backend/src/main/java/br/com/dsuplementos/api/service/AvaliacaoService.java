package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Avaliacao;
import br.com.dsuplementos.domain.FotoAvaliacao;
import br.com.dsuplementos.domain.Produto;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.dto.avaliacao.AvaliacaoRequest;
import br.com.dsuplementos.dto.avaliacao.AvaliacaoResponse;
import br.com.dsuplementos.dto.avaliacao.FotoAvaliacaoResponse;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.exception.RecursoNaoEncontradoException;
import br.com.dsuplementos.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ProdutoService produtoService;
    private final ArquivoService arquivoService;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            ProdutoService produtoService,
            ArquivoService arquivoService
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.produtoService = produtoService;
        this.arquivoService = arquivoService;
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listarPorProduto(Long produtoId) {
        produtoService.buscarEntidadeAtiva(produtoId);
        return avaliacaoRepository.findByProdutoIdOrderByCriadoEmDesc(produtoId).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public AvaliacaoResponse criar(Long produtoId, Usuario usuario, AvaliacaoRequest request) {
        if (avaliacaoRepository.findByProdutoIdAndUsuarioId(produtoId, usuario.getId()).isPresent()) {
            throw new RegraDeNegocioException("Voce ja avaliou este produto.");
        }

        Produto produto = produtoService.buscarEntidadeAtiva(produtoId);
        Avaliacao avaliacao = new Avaliacao(produto, usuario, request.nota(), request.comentario().trim());
        avaliacaoRepository.save(avaliacao);
        return paraResponse(avaliacao);
    }

    @Transactional
    public AvaliacaoResponse adicionarFotos(Long avaliacaoId, Usuario usuario, List<MultipartFile> arquivos) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliacao nao encontrada."));

        boolean eDono = avaliacao.getUsuario().getId().equals(usuario.getId());
        if (!eDono && usuario.getRole() != Role.ADMIN) {
            throw new RegraDeNegocioException("Voce nao pode alterar esta avaliacao.");
        }

        if (arquivos == null || arquivos.isEmpty()) {
            throw new RegraDeNegocioException("Selecione ao menos uma foto.");
        }
        if (avaliacao.getFotos().size() + arquivos.size() > 4) {
            throw new RegraDeNegocioException("Cada avaliacao pode ter no maximo 4 fotos.");
        }

        for (MultipartFile arquivo : arquivos) {
            String url = arquivoService.salvarFotoAvaliacao(arquivo);
            String nomeOriginal = arquivo.getOriginalFilename() == null ? "imagem" : arquivo.getOriginalFilename();
            avaliacao.adicionarFoto(new FotoAvaliacao(avaliacao, url, nomeOriginal));
        }

        avaliacaoRepository.save(avaliacao);
        return paraResponse(avaliacao);
    }

    private AvaliacaoResponse paraResponse(Avaliacao avaliacao) {
        List<FotoAvaliacaoResponse> fotos = avaliacao.getFotos().stream()
                .map(foto -> new FotoAvaliacaoResponse(foto.getId(), foto.getUrl(), foto.getNomeArquivo()))
                .toList();
        return new AvaliacaoResponse(
                avaliacao.getId(),
                primeiroNome(avaliacao.getUsuario().getNome()),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getCriadoEm(),
                fotos
        );
    }

    private String primeiroNome(String nome) {
        return nome.trim().split("\\s+")[0];
    }
}
