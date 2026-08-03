# DSuplementos Store

E-commerce de suplementos desenvolvido como projeto de portfolio e laboratório prático de Java, Spring Boot, banco de dados e frontend.

Sou formado em Análise e Desenvolvimento de Sistemas e estudo no Bootcamp Santander Java DIO 2026. A proposta deste repositório é transformar cada conteúdo estudado em uma parte funcional de um sistema, evoluindo de uma aplicação Java em memória para uma API real com PostgreSQL.

## O que esta versão adiciona

- API REST em Spring Boot 3.5
- PostgreSQL em nuvem preparado para Supabase
- Esquema SQL versionado com Flyway
- Cadastro e login reais com senha protegida por BCrypt
- Sessão com JWT, refresh token rotativo e logout revogável
- Perfis `CLIENTE` e `ADMIN`
- Perfil do cliente, troca de senha e endereços de entrega persistidos
- Carrinho vinculado ao usuário no banco de dados
- Checkout com frete simulado, reserva de estoque e histórico de pedidos
- Página de produto dinâmica com avaliações e fotos
- Dashboard de inventário protegido para o gestor

## Arquitetura

```text
Browser (HTML, CSS e JavaScript)
        |
        | fetch com token JWT
        v
Spring Boot API
        |
        | Spring Data JPA + Flyway
        v
PostgreSQL no Supabase
```

## Estrutura

```text
dsuplementos-store
├── database
│   ├── README.md
│   └── migrations
│       └── V1__cria_estrutura_inicial.sql
├── frontend
│   ├── index.html             # Home e catálogo
│   ├── produto.html           # Uma página reutilizável por produto
│   ├── login.html             # Login e cadastro
│   ├── conta.html             # Perfil autenticado
│   ├── checkout.html          # Confirmação de entrega e pedido
│   ├── pedidos.html           # Histórico e status dos pedidos
│   ├── dashboard.html         # Gestão de estoque, somente ADMIN
│   ├── api.js                 # Comunicação com a API e sessão JWT
│   ├── app.js
│   ├── produto.js
│   ├── auth.js
│   ├── dashboard.js
│   ├── conta.js
│   ├── style.css
│   └── assets/products
└── backend
    ├── pom.xml
    ├── .env.example
    └── src/main/java/br/com/dsuplementos
        ├── DsuplementosApplication.java
        ├── api                 # Controllers e serviços da API
        ├── config              # Segurança, CORS e dados iniciais
        ├── domain              # Entidades JPA e enums
        ├── dto                 # Dados recebidos e enviados pela API
        ├── repository          # Spring Data JPA
        └── security            # JWT
```

O `Main.java` e as classes em memória originais continuam no backend como laboratório de Collections. A API inicia por `DsuplementosApplication.java`.

## Banco de dados

O banco escolhido é PostgreSQL no Supabase. O plano gratuito é suficiente para o estudo, tem painel para visualizar tabelas e permite, numa etapa posterior, armazenar as fotos das avaliações no Storage.

1. Crie um projeto em [Supabase](https://supabase.com/).
2. Copie `backend/.env.example` para `backend/.env`.
3. Preencha `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` com valores privados.
4. Defina também os dados `APP_ADMIN_*` para criar a conta do gestor na primeira execução.

No macOS ou Linux, dentro de `backend`:

```bash
set -a
source .env
set +a
mvn spring-boot:run
```

O Flyway executará automaticamente todas as migrations em `database/migrations`, na ordem da versão. Nunca coloque o `.env` preenchido no Git.

## Rodando o frontend

Em outro terminal:

```bash
cd frontend
python3 -m http.server 5500
```

Abra [http://localhost:5500](http://localhost:5500). A API deve estar em `http://localhost:8080`.

## Fluxos disponíveis

### Cliente

1. Cria a conta em `login.html`.
2. Faz login e recebe um access token e refresh token.
3. Atualiza nome, senha e endereços em `conta.html`.
4. Adiciona produtos ao carrinho, que fica associado à conta no banco.
5. Define a entrega e cria o pedido pelo checkout. O produto e o endereço ficam registrados como uma cópia histórica da compra.
6. Acompanha o pedido, simula o pagamento e pode cancelá-lo enquanto ele ainda não foi separado.
7. Abre `produto.html?id=1`, publica uma avaliação e pode anexar fotos.

### Gestor

1. A conta é criada a partir das variáveis `APP_ADMIN_*`, não por um botão público.
2. Faz login pela mesma página de login.
3. É direcionado para `dashboard.html`.
4. Cadastra produtos, edita dados e atualiza estoque.

## Principais endpoints

| Método | Endpoint | Acesso | Finalidade |
| --- | --- | --- | --- |
| `POST` | `/api/auth/cadastro` | Público | Criar conta de cliente |
| `POST` | `/api/auth/login` | Público | Autenticar e receber uma sessão |
| `POST` | `/api/auth/refresh` | Público | Rotacionar refresh token e renovar a sessão |
| `POST` | `/api/auth/logout` | Público | Revogar um refresh token |
| `GET` | `/api/usuarios/me` | Autenticado | Consultar perfil da própria conta |
| `PUT` | `/api/usuarios/me` | Autenticado | Atualizar o próprio nome |
| `PATCH` | `/api/usuarios/me/senha` | Autenticado | Trocar senha e revogar sessões renováveis |
| `GET` | `/api/usuarios/me/enderecos` | Autenticado | Listar os próprios endereços |
| `POST` | `/api/usuarios/me/enderecos` | Autenticado | Criar endereço de entrega |
| `PUT` | `/api/usuarios/me/enderecos/{id}` | Dono | Atualizar o próprio endereço |
| `DELETE` | `/api/usuarios/me/enderecos/{id}` | Dono | Excluir o próprio endereço |
| `GET` | `/api/produtos` | Público | Listar catálogo |
| `GET` | `/api/produtos/{id}` | Público | Ver produto |
| `GET` | `/api/produtos/{id}/avaliacoes` | Público | Listar avaliações |
| `POST` | `/api/produtos/{id}/avaliacoes` | Cliente/Admin | Criar avaliação |
| `POST` | `/api/avaliacoes/{id}/fotos` | Dono/Admin | Enviar fotos da avaliação |
| `GET` | `/api/carrinho` | Autenticado | Ler carrinho da conta |
| `POST` | `/api/carrinho/itens` | Autenticado | Adicionar item |
| `GET` | `/api/pedidos/simulacao-frete` | Autenticado | Calcular frete a partir do carrinho |
| `POST` | `/api/pedidos` | Autenticado | Criar pedido e reservar estoque |
| `GET` | `/api/pedidos` | Autenticado | Listar histórico da própria conta |
| `POST` | `/api/pedidos/{id}/pagamento-simulado` | Dono | Simular confirmação de pagamento |
| `POST` | `/api/pedidos/{id}/cancelamento` | Dono | Cancelar e devolver itens ao estoque |
| `GET` | `/api/admin/produtos` | Admin | Inventário completo |
| `POST` | `/api/admin/produtos` | Admin | Cadastrar produto |
| `PUT` | `/api/admin/produtos/{id}` | Admin | Editar produto |
| `PATCH` | `/api/admin/produtos/{id}/estoque` | Admin | Alterar estoque |

## Próximos incrementos naturais

- Aplicar cupom e simular frete no checkout
- Mover arquivos de `backend/uploads` para Supabase Storage
- Adicionar paginação e busca feita pelo banco
- Aumentar a cobertura com testes de integração
- Conectar recursos de IA e voz estudados no bootcamp

## Autor

Gabriel Costa

Formado em Análise e Desenvolvimento de Sistemas. Estudante de Java e Spring Boot no Bootcamp Santander Java DIO 2026, com foco em construir aplicações completas e bem organizadas para portfolio.
