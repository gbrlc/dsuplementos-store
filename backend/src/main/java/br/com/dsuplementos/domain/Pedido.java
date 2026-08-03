package br.com.dsuplementos.domain;

import br.com.dsuplementos.domain.enums.StatusPedido;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cupom_id")
    private Cupom cupom;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal desconto;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal frete;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;
    @Column(name = "endereco_cep", length = 9)
    private String enderecoCep;
    @Column(name = "endereco_logradouro", length = 160)
    private String enderecoLogradouro;
    @Column(name = "endereco_numero", length = 20)
    private String enderecoNumero;
    @Column(name = "endereco_complemento", length = 100)
    private String enderecoComplemento;
    @Column(name = "endereco_bairro", length = 100)
    private String enderecoBairro;
    @Column(name = "endereco_cidade", length = 100)
    private String enderecoCidade;
    @Column(name = "endereco_estado", length = 2)
    private String enderecoEstado;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Pedido() {
    }

    public Pedido(Usuario usuario, Endereco endereco, BigDecimal subtotal, BigDecimal frete) {
        this.usuario = usuario;
        this.endereco = endereco;
        this.status = StatusPedido.CRIADO;
        this.subtotal = subtotal;
        this.desconto = BigDecimal.ZERO;
        this.frete = frete;
        this.total = subtotal.add(frete);
        copiarEndereco(endereco);
    }

    @PrePersist
    void criarDatas() {
        Instant agora = Instant.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void atualizarData() {
        atualizadoEm = Instant.now();
    }

    public void adicionarItem(ItemPedido item) {
        itens.add(item);
    }

    public void confirmarPagamento() {
        this.status = StatusPedido.PAGO;
    }

    public void cancelar() {
        this.status = StatusPedido.CANCELADO;
    }

    public boolean podeSerCancelado() {
        return status == StatusPedido.CRIADO || status == StatusPedido.PAGO;
    }

    private void copiarEndereco(Endereco endereco) {
        this.enderecoCep = endereco.getCep();
        this.enderecoLogradouro = endereco.getLogradouro();
        this.enderecoNumero = endereco.getNumero();
        this.enderecoComplemento = endereco.getComplemento();
        this.enderecoBairro = endereco.getBairro();
        this.enderecoCidade = endereco.getCidade();
        this.enderecoEstado = endereco.getEstado();
    }

    public Long getId() {
        return id;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getEnderecoCep() {
        return enderecoCep;
    }

    public String getEnderecoLogradouro() {
        return enderecoLogradouro;
    }

    public String getEnderecoNumero() {
        return enderecoNumero;
    }

    public String getEnderecoComplemento() {
        return enderecoComplemento;
    }

    public String getEnderecoBairro() {
        return enderecoBairro;
    }

    public String getEnderecoCidade() {
        return enderecoCidade;
    }

    public String getEnderecoEstado() {
        return enderecoEstado;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
