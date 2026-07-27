package br.com.dsuplementos.domain;

import br.com.dsuplementos.domain.enums.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private int estoque;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Produto() {
    }

    public Produto(
            String nome,
            Categoria categoria,
            Marca marca,
            BigDecimal preco,
            int estoque,
            String descricao,
            String imagemUrl
    ) {
        this.nome = nome;
        this.categoria = categoria;
        this.marca = marca;
        this.preco = preco;
        this.estoque = estoque;
        this.descricao = descricao;
        this.imagemUrl = imagemUrl;
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

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Marca getMarca() {
        return marca;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void atualizar(
            String nome,
            Categoria categoria,
            Marca marca,
            BigDecimal preco,
            int estoque,
            String descricao,
            String imagemUrl,
            boolean ativo
    ) {
        this.nome = nome;
        this.categoria = categoria;
        this.marca = marca;
        this.preco = preco;
        this.estoque = estoque;
        this.descricao = descricao;
        this.imagemUrl = imagemUrl;
        this.ativo = ativo;
    }

    public void atualizarEstoque(int estoque) {
        this.estoque = estoque;
    }
}
