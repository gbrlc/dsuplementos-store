# DSuplementos Store - Java

Projeto de estudo para simular a lógica de um e-commerce de suplementos usando Java puro.

A ideia é treinar uma estrutura parecida com a que será usada depois no Spring Boot:

```text
src/main/java/br/com/dsuplementos
├── Main.java
├── model
│   ├── Categoria.java
│   ├── Marca.java
│   └── Produto.java
├── repository
│   └── ProdutoRepository.java
├── service
│   ├── CarrinhoService.java
│   └── ProdutoService.java
└── util
    └── MoedaUtil.java
```

## O que este projeto pratica

- Cadastro de produtos em memória
- Listagem de produtos
- Busca por ID usando `Optional`
- Busca por categoria usando `Stream`
- Busca por faixa de preço usando `BigDecimal`
- Ordenação por preço usando `Comparator`
- Carrinho usando `Map<Long, Integer>`
- Categorias sem repetição usando `Set`

## Como executar

Dentro da pasta `dsuplementos-store-java`, rode:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out br.com.dsuplementos.Main
```

## Como isso conversa com seu frontend

Hoje seu site tem os produtos em JavaScript:

```js
let products = [
  { id: 1, nome: "Whey Protein", preco: 119.90, imagem: "assets/products/wheyintegral.jpeg" }
];
```

Neste projeto Java, esses produtos viram objetos:

```java
Produto whey = new Produto(
        1L,
        "Whey Protein",
        Categoria.WHEY,
        new Marca(1L, "Integralmedica"),
        new BigDecimal("119.90"),
        20,
        "Whey protein para uso diário"
);
```

Quando você chegar no Spring Boot, essa estrutura pode evoluir para uma API. O frontend deixará de ter a lista fixa no JavaScript e passará a buscar os produtos do backend.
