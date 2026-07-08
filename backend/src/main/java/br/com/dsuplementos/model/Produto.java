package br.com.dsuplementos.model;

import java.math.BigDecimal;

public class Produto {
    private Long id;
    private String nome;
    private Categoria categoria;
    private Marca marca;
    private BigDecimal preco;
    private int estoque;
    private String descricao;
    private String imagem;

    public Produto(
            Long id,
            String nome,
            Categoria categoria,
            Marca marca,
            BigDecimal preco,
            int estoque,
            String descricao,
            String imagem
    ) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.marca = marca;
        this.preco = preco;
        this.estoque = estoque;
        this.descricao = descricao;
        this.imagem = imagem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", categoria=" + categoria +
                ", marca=" + marca +
                ", preco=" + preco +
                ", estoque=" + estoque +
                '}';
    }
}
