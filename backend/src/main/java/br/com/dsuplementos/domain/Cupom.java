package br.com.dsuplementos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cupons")
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;
    @Column(name = "percentual_desconto", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualDesconto;
    @Column(name = "valor_minimo", precision = 12, scale = 2)
    private BigDecimal valorMinimo;
    @Column(name = "limite_uso")
    private Integer limiteUso;
    @Column(nullable = false)
    private int usos;
    @Column(name = "valido_de")
    private Instant validoDe;
    @Column(name = "valido_ate")
    private Instant validoAte;
    @Column(nullable = false)
    private boolean ativo = true;

    protected Cupom() {
    }
}
