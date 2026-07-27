package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Marca;
import br.com.dsuplementos.domain.Produto;
import br.com.dsuplementos.domain.Avaliacao;
import br.com.dsuplementos.domain.enums.Categoria;
import br.com.dsuplementos.dto.produto.MarcaResponse;
import br.com.dsuplementos.dto.produto.ProdutoRequest;
import br.com.dsuplementos.dto.produto.ProdutoResponse;
import br.com.dsuplementos.exception.RecursoNaoEncontradoException;
import br.com.dsuplementos.repository.AvaliacaoRepository;
import br.com.dsuplementos.repository.MarcaRepository;
import br.com.dsuplementos.repository.ProdutoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoJpaRepository produtoRepository;
    private final MarcaRepository marcaRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public ProdutoService(
            ProdutoJpaRepository produtoRepository,
            MarcaRepository marcaRepository,
            AvaliacaoRepository avaliacaoRepository
    ) {
        this.produtoRepository = produtoRepository;
        this.marcaRepository = marcaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(Categoria categoria, BigDecimal precoMinimo, BigDecimal precoMaximo, String ordenar) {
        return ordenar(produtoRepository.findByAtivoTrue(), ordenar).stream()
                .filter(produto -> categoria == null || produto.getCategoria() == categoria)
                .filter(produto -> precoMinimo == null || produto.getPreco().compareTo(precoMinimo) >= 0)
                .filter(produto -> precoMaximo == null || produto.getPreco().compareTo(precoMaximo) <= 0)
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return paraResponse(buscarEntidadeAtiva(id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarInventario() {
        return produtoRepository.findAllByOrderByNomeAsc().stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MarcaResponse> listarMarcas() {
        return marcaRepository.findAll().stream()
                .sorted(Comparator.comparing(Marca::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(marca -> new MarcaResponse(marca.getId(), marca.getNome()))
                .toList();
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Marca marca = buscarMarca(request.marcaId());
        Produto produto = new Produto(
                request.nome().trim(),
                request.categoria(),
                marca,
                request.preco(),
                request.estoque(),
                request.descricao().trim(),
                normalizarImagem(request.imagemUrl())
        );
        produtoRepository.save(produto);
        return paraResponse(produto);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidade(id);
        Marca marca = buscarMarca(request.marcaId());
        produto.atualizar(
                request.nome().trim(),
                request.categoria(),
                marca,
                request.preco(),
                request.estoque(),
                request.descricao().trim(),
                normalizarImagem(request.imagemUrl()),
                request.ativo() == null || request.ativo()
        );
        return paraResponse(produto);
    }

    @Transactional
    public ProdutoResponse atualizarEstoque(Long id, int estoque) {
        Produto produto = buscarEntidade(id);
        produto.atualizarEstoque(estoque);
        return paraResponse(produto);
    }

    public Produto buscarEntidadeAtiva(Long id) {
        return produtoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado."));
    }

    private Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado."));
    }

    private Marca buscarMarca(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Marca nao encontrada."));
    }

    private ProdutoResponse paraResponse(Produto produto) {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByProdutoIdOrderByCriadoEmDesc(produto.getId());
        double media = avaliacoes.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);

        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getCategoria(),
                produto.getMarca().getId(),
                produto.getMarca().getNome(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.getDescricao(),
                produto.getImagemUrl(),
                produto.isAtivo(),
                BigDecimal.valueOf(media).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                avaliacoes.size()
        );
    }

    private List<Produto> ordenar(List<Produto> produtos, String ordenar) {
        Comparator<Produto> comparador = switch (ordenar == null ? "" : ordenar) {
            case "preco_asc" -> Comparator.comparing(Produto::getPreco);
            case "preco_desc" -> Comparator.comparing(Produto::getPreco).reversed();
            case "nome" -> Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Produto::getId);
        };
        return produtos.stream().sorted(comparador).toList();
    }

    private String normalizarImagem(String imagemUrl) {
        return imagemUrl == null || imagemUrl.isBlank() ? null : imagemUrl.trim();
    }
}
