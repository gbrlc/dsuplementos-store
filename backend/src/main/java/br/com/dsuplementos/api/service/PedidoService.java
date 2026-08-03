package br.com.dsuplementos.api.service;

import br.com.dsuplementos.domain.Carrinho;
import br.com.dsuplementos.domain.Endereco;
import br.com.dsuplementos.domain.ItemCarrinho;
import br.com.dsuplementos.domain.ItemPedido;
import br.com.dsuplementos.domain.Pedido;
import br.com.dsuplementos.domain.Produto;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.StatusPedido;
import br.com.dsuplementos.dto.pedido.CheckoutRequest;
import br.com.dsuplementos.dto.pedido.EnderecoPedidoResponse;
import br.com.dsuplementos.dto.pedido.FreteResponse;
import br.com.dsuplementos.dto.pedido.ItemPedidoResponse;
import br.com.dsuplementos.dto.pedido.PedidoResponse;
import br.com.dsuplementos.exception.RegraDeNegocioException;
import br.com.dsuplementos.exception.RecursoNaoEncontradoException;
import br.com.dsuplementos.repository.CarrinhoRepository;
import br.com.dsuplementos.repository.EnderecoRepository;
import br.com.dsuplementos.repository.PedidoRepository;
import br.com.dsuplementos.repository.ProdutoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private static final BigDecimal VALOR_FRETE = new BigDecimal("19.90");
    private static final BigDecimal LIMITE_FRETE_GRATIS = new BigDecimal("199.00");

    private final PedidoRepository pedidoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final EnderecoRepository enderecoRepository;
    private final ProdutoJpaRepository produtoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            CarrinhoRepository carrinhoRepository,
            EnderecoRepository enderecoRepository,
            ProdutoJpaRepository produtoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.enderecoRepository = enderecoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public FreteResponse simularFrete(Usuario usuario) {
        BigDecimal subtotal = calcularSubtotal(obterCarrinhoComItens(usuario));
        BigDecimal frete = calcularFrete(subtotal);
        return new FreteResponse(subtotal, frete, subtotal.add(frete));
    }

    @Transactional
    public PedidoResponse criarPedido(Usuario usuario, CheckoutRequest request) {
        Carrinho carrinho = obterCarrinhoComItens(usuario);
        Endereco endereco = enderecoRepository.findByIdAndUsuarioId(request.enderecoId(), usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereco nao encontrado."));

        List<ItemCarrinho> itensCarrinho = List.copyOf(carrinho.getItens());
        if (itensCarrinho.isEmpty()) {
            throw new RegraDeNegocioException("O carrinho esta vazio.");
        }

        List<ProdutoSelecionado> produtosSelecionados = itensCarrinho.stream()
                .map(this::reservarProdutoComEstoqueBloqueado)
                .toList();
        BigDecimal subtotal = produtosSelecionados.stream()
                .map(item -> item.produto().getPreco().multiply(BigDecimal.valueOf(item.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Pedido pedido = new Pedido(usuario, endereco, subtotal, calcularFrete(subtotal));
        produtosSelecionados.forEach(item -> pedido.adicionarItem(new ItemPedido(
                pedido,
                item.produto(),
                item.quantidade()
        )));

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        carrinho.limparItens();
        return paraResponse(pedidoSalvo);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Usuario usuario) {
        return pedidoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuario.getId())
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public PedidoResponse simularPagamento(Usuario usuario, Long pedidoId) {
        Pedido pedido = buscarPedidoDoUsuario(pedidoId, usuario.getId());
        if (pedido.getStatus() != StatusPedido.CRIADO) {
            throw new RegraDeNegocioException("Apenas pedidos aguardando pagamento podem ser pagos.");
        }
        pedido.confirmarPagamento();
        return paraResponse(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(Usuario usuario, Long pedidoId) {
        Pedido pedido = buscarPedidoDoUsuario(pedidoId, usuario.getId());
        if (!pedido.podeSerCancelado()) {
            throw new RegraDeNegocioException("Este pedido nao pode mais ser cancelado.");
        }

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            if (produto != null) {
                Produto produtoBloqueado = produtoRepository.findByIdComBloqueio(produto.getId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Produto do pedido nao encontrado."));
                produtoBloqueado.atualizarEstoque(produtoBloqueado.getEstoque() + item.getQuantidade());
            }
        }
        pedido.cancelar();
        return paraResponse(pedido);
    }

    private Carrinho obterCarrinhoComItens(Usuario usuario) {
        Carrinho carrinho = carrinhoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegraDeNegocioException("O carrinho esta vazio."));
        if (carrinho.getItens().isEmpty()) {
            throw new RegraDeNegocioException("O carrinho esta vazio.");
        }
        return carrinho;
    }

    private ProdutoSelecionado reservarProdutoComEstoqueBloqueado(ItemCarrinho itemCarrinho) {
        Produto produto = produtoRepository.findByIdComBloqueio(itemCarrinho.getProduto().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado."));
        if (!produto.isAtivo()) {
            throw new RegraDeNegocioException("Um produto do carrinho nao esta mais disponivel.");
        }
        if (produto.getEstoque() < itemCarrinho.getQuantidade()) {
            throw new RegraDeNegocioException("Estoque insuficiente para " + produto.getNome() + ".");
        }
        produto.atualizarEstoque(produto.getEstoque() - itemCarrinho.getQuantidade());
        return new ProdutoSelecionado(produto, itemCarrinho.getQuantidade());
    }

    private BigDecimal calcularSubtotal(Carrinho carrinho) {
        return carrinho.getItens().stream()
                .map(item -> item.getProduto().getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularFrete(BigDecimal subtotal) {
        return subtotal.compareTo(LIMITE_FRETE_GRATIS) >= 0 ? BigDecimal.ZERO : VALOR_FRETE;
    }

    private Pedido buscarPedidoDoUsuario(Long pedidoId, Long usuarioId) {
        return pedidoRepository.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido nao encontrado."));
    }

    private PedidoResponse paraResponse(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(item -> new ItemPedidoResponse(
                        item.getProduto() == null ? null : item.getProduto().getId(),
                        item.getNomeProduto(),
                        item.getPrecoUnitario(),
                        item.getQuantidade(),
                        item.getSubtotal()
                ))
                .toList();
        EnderecoPedidoResponse endereco = new EnderecoPedidoResponse(
                pedido.getEnderecoCep(),
                pedido.getEnderecoLogradouro(),
                pedido.getEnderecoNumero(),
                pedido.getEnderecoComplemento(),
                pedido.getEnderecoBairro(),
                pedido.getEnderecoCidade(),
                pedido.getEnderecoEstado()
        );
        return new PedidoResponse(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getSubtotal(),
                pedido.getDesconto(),
                pedido.getFrete(),
                pedido.getTotal(),
                endereco,
                itens,
                pedido.getCriadoEm()
        );
    }

    private record ProdutoSelecionado(Produto produto, int quantidade) {
    }
}
