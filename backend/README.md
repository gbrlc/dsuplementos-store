# DSuplementos Store - Backend Java

Backend em Java puro para simular a regra de negocio de um e-commerce de suplementos.

Este projeto nasceu como uma evolucao do frontend da **DSuplementos Store** e faz parte do meu processo de estudo no **Bootcamp Santander Java DIO 2026**. Sou formado em **Analise e Desenvolvimento de Sistemas** e estou aprofundando meus conhecimentos em Java, Collections, Streams, BigDecimal, Optional e Spring Boot.

O objetivo desta etapa e construir uma base organizada antes de adicionar banco de dados, API REST e integracao real com o frontend.

## Status do Projeto

Projeto em desenvolvimento.

Etapa atual:

- Backend em Java puro
- Dados armazenados em memoria
- Regras de negocio separadas por camada
- Estrutura preparada para futura migracao para Spring Boot

Proximas etapas:

- Criar API REST com Spring Boot
- Conectar o frontend usando `fetch`
- Substituir dados em memoria por banco de dados
- Evoluir o projeto para recursos mais avancados com IA e voz

## Tecnologias e Conceitos

- Java
- Programacao Orientada a Objetos
- Collections Framework
- `List`
- `Set`
- `Map`
- `Stream API`
- `Optional`
- `BigDecimal`
- `Comparator`
- Organizacao em camadas

## Funcionalidades Implementadas

- Cadastrar produtos
- Listar produtos
- Buscar produto por ID usando `Optional`
- Buscar produtos por categoria usando `Stream`
- Buscar produtos por faixa de preco usando `BigDecimal`
- Ordenar produtos por menor preco usando `Comparator`
- Adicionar produtos ao carrinho usando `Map<Long, Integer>`
- Listar categorias cadastradas sem repeticao usando `Set`
- Calcular total do carrinho com precisao monetaria

## Estrutura do Projeto

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

## Papel de Cada Camada

### `model`

Contem as classes que representam os dados principais do sistema.

- `Produto`: representa um suplemento vendido na loja.
- `Marca`: representa a marca do produto.
- `Categoria`: enum usado para evitar categorias digitadas de formas diferentes.

### `repository`

Simula a camada de acesso a dados.

No momento, os produtos ficam em uma lista em memoria. Futuramente, essa camada podera ser substituida por um repository do Spring Data JPA.

### `service`

Contem as regras de negocio.

- `ProdutoService`: cadastro, buscas, filtros, ordenacao e categorias.
- `CarrinhoService`: controle de itens, quantidades e total do carrinho.

### `util`

Contem recursos auxiliares do projeto.

- `MoedaUtil`: formata valores em reais.

## Como Executar

Entre na pasta do backend:

```bash
cd backend
```

Compile o projeto:

```bash
javac -d out $(find src/main/java -name "*.java")
```

Execute:

```bash
java -cp out br.com.dsuplementos.Main
```

## Exemplo de Saida

```text
=== Todos os produtos ===
Produto{id=1, nome='Whey Protein', categoria=WHEY, marca=Integralmedica, preco=119.90, estoque=20}

=== Produtos da categoria CREATINA ===
Produto{id=2, nome='Creatina', categoria=CREATINA, marca=Absolut Nutrition, preco=89.90, estoque=15}

=== Carrinho ===
Whey Protein x 2
Creatina x 1
Total: R$ 329,70
```

## Relacao com o Frontend

Atualmente, o frontend da loja possui os produtos cadastrados diretamente no JavaScript:

```js
let products = [
  { id: 1, nome: "Whey Protein", preco: 119.90, imagem: "assets/products/wheyintegral.jpeg" }
];
```

Neste backend, os produtos passam a ser representados como objetos Java:

```java
Produto whey = new Produto(
        1L,
        "Whey Protein",
        Categoria.WHEY,
        new Marca(1L, "Integralmedica"),
        new BigDecimal("119.90"),
        20,
        "Whey protein para uso diario.",
        "assets/products/wheyintegral.jpeg"
);
```

A ideia e que, futuramente, o frontend deixe de usar uma lista fixa no JavaScript e passe a consumir os produtos de uma API criada com Spring Boot.

Fluxo esperado na proxima fase:

```text
Frontend
   |
   | fetch("/produtos")
   v
Spring Boot API
   |
   v
Banco de Dados
```

## Evolucao para Spring Boot

Esta estrutura ja foi pensada para facilitar a migracao:

```text
Java puro agora                 Spring Boot depois
Produto.java              ->    Entity Produto
ProdutoRepository.java    ->    Spring Data JPA Repository
ProdutoService.java       ->    Service com regras de negocio
Main.java                 ->    Controller + endpoints REST
Lista em memoria          ->    Banco de dados
```

Endpoints planejados:

```text
GET    /produtos
GET    /produtos/{id}
GET    /produtos/categoria/{categoria}
POST   /produtos
POST   /carrinho/itens
GET    /carrinho
```

## Aprendizados Aplicados

Este projeto aplica diretamente conteudos estudados no bootcamp:

- Uso de `Set` para evitar repeticao de categorias
- Uso de `Map` para representar produto e quantidade no carrinho
- Uso de `BigDecimal` para valores monetarios
- Uso de `Optional` para buscas que podem nao retornar resultado
- Uso de `Stream` para filtros e ordenacoes
- Separacao de responsabilidades entre model, repository e service

## Autor

Gabriel Costa

Formado em Analise e Desenvolvimento de Sistemas. Atualmente estudando Java e Spring Boot pelo Bootcamp Santander Java DIO 2026, com foco em evoluir este projeto para uma aplicacao completa com backend, frontend, banco de dados e recursos de IA.
