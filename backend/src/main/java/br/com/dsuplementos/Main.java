package br.com.dsuplementos;

import br.com.dsuplementos.model.Categoria;
import br.com.dsuplementos.model.Marca;
import br.com.dsuplementos.model.Produto;
import br.com.dsuplementos.repository.ProdutoRepository;
import br.com.dsuplementos.service.CarrinhoService;
import br.com.dsuplementos.service.ProdutoService;
import br.com.dsuplementos.util.MoedaUtil;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        ProdutoRepository produtoRepository = new ProdutoRepository();
        ProdutoService produtoService = new ProdutoService(produtoRepository);
        CarrinhoService carrinhoService = new CarrinhoService(produtoService);

        cadastrarProdutosDeExemplo(produtoService);

        System.out.println("=== Todos os produtos ===");
        produtoService.listarTodos().forEach(System.out::println);

        System.out.println("\n=== Buscar produto por ID ===");
        produtoService.buscarPorId(1L).ifPresent(System.out::println);

        System.out.println("\n=== Produtos da categoria CREATINA ===");
        produtoService.buscarPorCategoria(Categoria.CREATINA).forEach(System.out::println);

        System.out.println("\n=== Produtos entre R$ 80,00 e R$ 100,00 ===");
        produtoService.buscarPorFaixaDePreco(new BigDecimal("80.00"), new BigDecimal("100.00"))
                .forEach(System.out::println);

        System.out.println("\n=== Produtos ordenados por menor preco ===");
        produtoService.ordenarPorMenorPreco().forEach(System.out::println);

        System.out.println("\n=== Categorias cadastradas sem repeticao ===");
        produtoService.listarCategoriasCadastradas().forEach(System.out::println);

        System.out.println("\n=== Carrinho ===");
        carrinhoService.adicionarProduto(1L, 2);
        carrinhoService.adicionarProduto(2L, 1);
        carrinhoService.listarItens().forEach((produto, quantidade) ->
                System.out.println(produto.getNome() + " x " + quantidade)
        );
        System.out.println("Total: " + MoedaUtil.formatar(carrinhoService.calcularTotal()));
    }

    private static void cadastrarProdutosDeExemplo(ProdutoService produtoService) {
        Marca integralmedica = new Marca(1L, "Integralmedica");
        Marca absolut = new Marca(2L, "Absolut Nutrition");
        Marca darkLab = new Marca(3L, "Dark Lab");
        Marca maxTitanium = new Marca(4L, "Max Titanium");

        produtoService.cadastrar(new Produto(
                1L,
                "Whey Protein",
                Categoria.WHEY,
                integralmedica,
                new BigDecimal("119.90"),
                20,
                "Whey protein para uso diario.",
                "assets/products/wheyintegral.jpeg"
        ));

        produtoService.cadastrar(new Produto(
                2L,
                "Creatina",
                Categoria.CREATINA,
                absolut,
                new BigDecimal("89.90"),
                15,
                "Creatina monohidratada.",
                "assets/products/creatinaabsolut.jpeg"
        ));

        produtoService.cadastrar(new Produto(
                3L,
                "Bone Crusher",
                Categoria.COLAGENO,
                darkLab,
                new BigDecimal("89.90"),
                10,
                "Suplemento para suporte articular.",
                "assets/products/colageno2.jpeg"
        ));

        produtoService.cadastrar(new Produto(
                4L,
                "Colageno",
                Categoria.COLAGENO,
                maxTitanium,
                new BigDecimal("89.90"),
                12,
                "Colageno em po.",
                "assets/products/colageno.jpeg"
        ));

        produtoService.cadastrar(new Produto(
                5L,
                "Creatina Black",
                Categoria.CREATINA,
                darkLab,
                new BigDecimal("89.90"),
                18,
                "Creatina premium.",
                "assets/products/creatina3.jpeg"
        ));

        produtoService.cadastrar(new Produto(
                6L,
                "Multi",
                Categoria.VITAMINAS,
                integralmedica,
                new BigDecimal("79.90"),
                25,
                "Multivitaminico.",
                "assets/products/multi.jpeg"
        ));
    }
}
