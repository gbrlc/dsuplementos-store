# DSuplementos Store

Projeto de e-commerce de suplementos desenvolvido como parte da minha evolucao em Java, frontend e backend.

Sou formado em **Analise e Desenvolvimento de Sistemas** e atualmente estou estudando no **Bootcamp Santander Java DIO 2026**. Este repositorio representa minha tentativa de transformar os conteudos do bootcamp em um projeto pratico de portfolio, evoluindo aos poucos de um frontend estatico para uma aplicacao completa com Java, Spring Boot, banco de dados e futuramente recursos de IA.

## Objetivo

Construir uma loja de suplementos com separacao entre frontend e backend, aplicando conceitos reais de desenvolvimento:

- Organizacao de projeto
- Modelagem de entidades
- Regras de negocio
- Carrinho de compras
- Simulacao de persistencia em memoria
- Preparacao para futura API REST com Spring Boot

## Status

Projeto em desenvolvimento.

Atualmente o repositorio possui:

- Frontend com HTML, CSS e JavaScript
- Produtos renderizados na tela
- Carrinho com `localStorage`
- Login simples usando `localStorage`
- Backend inicial em Java puro
- Modelagem de produtos, marcas e categorias
- Regras de negocio usando Collections, Streams, Optional e BigDecimal

## Estrutura

```text
dsuplementos-store
├── frontend
│   ├── index.html
│   ├── style.css
│   ├── app.js
│   └── assets
│       └── products
│
└── backend
    ├── README.md
    └── src/main/java/br/com/dsuplementos
        ├── Main.java
        ├── model
        ├── repository
        ├── service
        └── util
```

## Frontend

O frontend representa a vitrine da loja.

Funcionalidades atuais:

- Listagem de produtos
- Imagem, nome e preco de cada produto
- Botao para adicionar ao carrinho
- Carrinho salvo no navegador com `localStorage`
- Login simples para simular usuario logado
- Finalizacao de compra simulada

Tecnologias:

- HTML
- CSS
- JavaScript
- LocalStorage

## Backend

O backend foi iniciado em Java puro para praticar a base antes da entrada completa em Spring Boot.

Funcionalidades atuais:

- Cadastro de produtos em memoria
- Listagem de produtos
- Busca por ID com `Optional`
- Busca por categoria com `Stream`
- Busca por faixa de preco com `BigDecimal`
- Ordenacao por menor preco com `Comparator`
- Carrinho com `Map<Long, Integer>`
- Categorias sem repeticao com `Set`

Conceitos aplicados:

- Programacao Orientada a Objetos
- Collections Framework
- `List`
- `Set`
- `Map`
- `Stream API`
- `Optional`
- `BigDecimal`
- Separacao em camadas

## Como Rodar o Frontend

Abra o arquivo abaixo no navegador:

```text
frontend/index.html
```

Ou use a extensao Live Server no VS Code.

## Como Rodar o Backend Java

Entre na pasta do backend:

```bash
cd backend
```

Compile:

```bash
javac -d out $(find src/main/java -name "*.java")
```

Execute:

```bash
java -cp out br.com.dsuplementos.Main
```

## Relacao com Meus Estudos

Este projeto acompanha minha evolucao no Bootcamp Santander Java DIO 2026.

No momento, estou estudando:

- Collections
- `Set`
- `Map`
- Wrappers
- `BigDecimal`
- Enums
- `Optional`
- Streams
- Generics
- Spring Boot

A ideia e aplicar cada conteudo novo neste repositorio, em vez de deixar os estudos apenas em exemplos isolados.

## Proximos Passos

- Melhorar a organizacao visual do frontend
- Corrigir o carrinho para agrupar produto e quantidade
- Criar endpoints com Spring Boot
- Fazer o frontend consumir o backend usando `fetch`
- Adicionar banco de dados
- Criar cadastro real de usuarios
- Criar fluxo de pedidos
- Evoluir o projeto para usar IA com leitura ou interacao por voz

## Planejamento de API

Endpoints que devem ser criados futuramente:

```text
GET    /produtos
GET    /produtos/{id}
GET    /produtos/categoria/{categoria}
POST   /produtos
GET    /carrinho
POST   /carrinho/itens
DELETE /carrinho/itens/{produtoId}
POST   /pedidos
```

## Autor

Gabriel Costa

Formado em Analise e Desenvolvimento de Sistemas. Estudando Java e Spring Boot pelo Bootcamp Santander Java DIO 2026, com foco em construir projetos praticos de portfolio e evoluir para aplicacoes completas com backend, frontend, banco de dados e recursos de IA.
