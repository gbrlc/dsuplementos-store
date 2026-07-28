# Backend - DSuplementos API

API REST criada com Spring Boot para substituir os dados em memória por PostgreSQL e servir o frontend da DSuplementos Store.

## Duas etapas convivendo aqui

- `Main.java`, `model`, `service` e `repository` originais: exercício de Java puro, Collections, Stream, Optional, Set, Map e BigDecimal.
- `DsuplementosApplication.java`, `domain`, `api`, `security` e os novos repositories: aplicação Spring Boot que sobe um servidor HTTP na porta 8080.

Manter as duas etapas ajuda a enxergar a evolução sem apagar o que foi aprendido antes.

## Camadas da API

```text
Controller -> recebe HTTP e devolve JSON
Service    -> aplica regras de negócio
Repository -> conversa com PostgreSQL pelo Spring Data JPA
Entity     -> representa as tabelas do banco
DTO        -> define o contrato da API
Mapper     -> transforma Entity em DTO com MapStruct
```

## Segurança

1. No cadastro, a senha é transformada em hash BCrypt.
2. No login, a API compara a senha enviada com o hash salvo no PostgreSQL.
3. Se estiver correta, a API envia um JWT.
4. O frontend manda o JWT no header `Authorization: Bearer <token>`.
5. O Spring Security permite ou bloqueia a rota conforme o perfil do usuário.

O frontend não decide quem pode usar o dashboard. Mesmo que alguém tente chamar a URL diretamente, o backend responde `403` sem o perfil `ADMIN`.

## Configuração

Use o arquivo `.env.example` como modelo e preencha um `.env` local. Ele fica fora do Git.

```bash
cd backend
cp .env.example .env
set -a
source .env
set +a
mvn spring-boot:run
```

Você precisa ter Java 21 ou superior e Maven instalados. O computador atual já possui Maven e Java 26, que executam o projeto configurado para Java 21.

### Perfis de execucao

- `dev`: perfil padrao. Mostra as consultas SQL e mantem o catalogo inicial habilitado.
- `prod`: desabilita o catalogo de exemplo, nao mostra SQL e bloqueia a pagina Swagger por padrao.

Defina o perfil no `.env`:

```env
SPRING_PROFILES_ACTIVE=dev
```

Para subir como producao, use `SPRING_PROFILES_ACTIVE=prod` e as variaveis reais de banco e JWT no ambiente de deploy.

## Documentacao da API

Com a API rodando no perfil `dev`, abra [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

O Swagger gera a documentacao a partir dos controllers e DTOs, e possui o botao `Authorize` para testar rotas protegidas com o JWT recebido no login. A especificacao em JSON fica em [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

As falhas agora seguem o mesmo formato, preservando a mensagem que o frontend ja utiliza:

```json
{
  "status": 401,
  "codigo": "AUTENTICACAO_NECESSARIA",
  "mensagem": "Autenticacao necessaria ou expirada.",
  "caminho": "/api/carrinho"
}
```

## Banco e migrations

O Flyway lê a migration em `../database/migrations` durante o build e cria as tabelas quando a aplicação inicia. A configuração `ddl-auto: validate` diz ao Hibernate para conferir se as entities Java correspondem ao SQL, sem deixar que ele invente alterações no banco.

## Teste rápido da API

Depois de iniciar a aplicação:

```bash
curl http://localhost:8080/api/produtos
```

O catálogo inicial é carregado automaticamente no primeiro início. Para criar o administrador, mantenha `APP_ADMIN_ENABLED=true` e preencha as três variáveis `APP_ADMIN_*` antes de iniciar.
