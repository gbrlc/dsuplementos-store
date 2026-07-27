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
@Table(name = "enderecos")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String apelido;
    @Column(nullable = false, length = 9)
    private String cep;
    @Column(nullable = false, length = 160)
    private String logradouro;
    @Column(nullable = false, length = 20)
    private String numero;
    private String complemento;
    @Column(nullable = false)
    private String bairro;
    @Column(nullable = false)
    private String cidade;
    @Column(nullable = false, length = 2)
    private String estado;
    @Column(nullable = false)
    private boolean principal;
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Endereco() {
    }

    @PrePersist
    void criarData() {
        criadoEm = Instant.now();
    }
}
