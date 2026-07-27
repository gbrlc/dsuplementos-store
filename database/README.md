# Banco de dados

Este diretorio guarda o esquema versionado do PostgreSQL. O arquivo SQL e uma **migration do Flyway**: a API o executa uma unica vez quando inicia em um banco vazio e registra a execucao na tabela `flyway_schema_history`.

## Por que nao criar tabelas manualmente?

Porque banco tambem e codigo. Versionar a estrutura permite que qualquer pessoa do projeto tenha as mesmas tabelas e relacionamentos, tanto no computador quanto no Supabase.

## Entidades modeladas

```text
Usuario 1 --- N Endereco
Usuario 1 --- 1 Carrinho 1 --- N ItemCarrinho N --- 1 Produto
Usuario 1 --- N Pedido 1 --- N ItemPedido
Produto N --- 1 Marca
Produto 1 --- N Avaliacao N --- 1 Usuario
Avaliacao 1 --- N FotoAvaliacao
Pedido N --- 0..1 Cupom
```

## Criando o banco gratuito no Supabase

1. Crie um projeto em [Supabase](https://supabase.com/).
2. Em **Connect**, copie os dados de conexao PostgreSQL.
3. Crie `backend/.env` usando `backend/.env.example` como modelo. Esse arquivo fica ignorado pelo Git.
4. Execute a API. O Flyway criara as tabelas automaticamente.

O plano gratuito inclui Postgres e 1 GB de Storage; o projeto pode ser pausado depois de inatividade. Para esta primeira versao, as fotos de avaliacoes ficam em `backend/uploads` enquanto a API esta rodando. A proxima melhoria natural e enviar esses arquivos ao Supabase Storage sem expor nenhuma chave no frontend.
