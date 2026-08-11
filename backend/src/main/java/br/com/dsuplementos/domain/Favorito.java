package br.com.dsuplementos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "favoritos",
        uniqueConstraints = @UniqueConstraint(name = "uk_favorito_usuario_produto", columnNames = {"usuario_id", "produto_id"})
)
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Favorito() {
    }

    public Favorito(Usuario usuario, Produto produto) {
        this.usuario = usuario;
        this.produto = produto;
    }

    @PrePersist
    void criarData() {
        criadoEm = Instant.now();
    }

    public Produto getProduto() {
        return produto;
    }
}
