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

import java.time.Instant;

@Entity
@Table(name = "fotos_avaliacao")
public class FotoAvaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected FotoAvaliacao() {
    }

    public FotoAvaliacao(Avaliacao avaliacao, String url, String nomeArquivo) {
        this.avaliacao = avaliacao;
        this.url = url;
        this.nomeArquivo = nomeArquivo;
    }

    @PrePersist
    void criarData() {
        criadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }
}
