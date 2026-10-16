# DSuplementos Store

Projeto desenvolvido e entregue para um pequeno comércio de suplementos, com catálogo online e uma API em Java 21 e Spring Boot.

No uso informado pelo cliente, o consumidor consulta o catálogo, solicita o pedido por e-mail e conclui a venda pelo WhatsApp. **Não há pagamento online integrado e um gateway de pagamento não faz parte da necessidade do negócio.**

[Catálogo publicado](https://dsuplementos-store-2.onrender.com/index.html) · [Backend e API](backend/README.md) · [Arquitetura](backend/ARCHITECTURE.md) · [Decisões técnicas](docs/decisoes.md)

## Uso real e escopo do repositório

| Contexto | Descrição |
| --- | --- |
| Operação do cliente | Catálogo → solicitação por e-mail → atendimento e fechamento pelo WhatsApp, conforme contexto informado pelo responsável pelo projeto |
| Implementação presente | API de catálogo, usuários, endereços, carrinho, pedidos, estoque, favoritos e avaliações; interface HTML/CSS/JavaScript |
| Recursos demonstrativos | Simulação de frete e pagamento, sem integração financeira. O estado `PAGO` pode ser marcado pelo próprio cliente no fluxo simulado e não comprova recebimento de dinheiro |
| Integração ainda não identificada | O mecanismo de envio de solicitações por e-mail e encaminhamento ao WhatsApp não foi localizado neste código. Não se atribui esse envio à API sem evidência |
| Mensageria opcional | Publicação RabbitMQ de `pedido.criado`, desabilitada por padrão; não envia e-mail nem conclui vendas |

A API reserva estoque ao criar um pedido e o devolve em cancelamentos permitidos. Esse comportamento técnico não equivale a uma venda concluída pelo WhatsApp. A conciliação com vendas externas e o prazo de reserva precisam ser definidos com o comércio antes de usar esse fluxo como controle operacional definitivo.

## Stack e organização

- Java 21, Spring Boot, Spring MVC, Spring Data JPA e PostgreSQL.
- Spring Security, JWT, BCrypt e refresh tokens opacos persistidos por hash.
- Flyway, Bean Validation, DTOs e MapStruct.
- JUnit 5, Mockito, MockMvc e Testcontainers para PostgreSQL.
- Docker multi-stage e workflow GitHub Actions.
- Frontend HTML, CSS e JavaScript, servido separadamente da API.

```text
frontend/                 catálogo e interface web
backend/src/main/java/    controllers, services, domínio, segurança e repositories
backend/src/test/java/    testes unitários, HTTP e integração
backend/src/main/resources/ configurações por ambiente
database/migrations/      migrations Flyway
.github/workflows/ci.yml  testes e build da imagem
Dockerfile                imagem somente do backend
compose.yaml              API, PostgreSQL e RabbitMQ opcional
```

Classes em `model`, `service` e `Main.java` preservam um exercício anterior de Java puro. A API inicia por `DsuplementosApplication` e usa `domain` e `api/service`.

## Execução local

### Java e PostgreSQL

Requer Java 21 e Maven 3.9, além de PostgreSQL acessível.

```bash
cd backend
cp .env.example .env
# Edite .env com seus valores. Não versione credenciais.
set -a
source .env
set +a
mvn spring-boot:run
```

O arquivo `.env` deve usar sintaxe compatível com o shell, com aspas quando necessárias. Use um banco de desenvolvimento. O Flyway aplica migrations na inicialização; o Hibernate valida o schema.

Em outro terminal, na raiz do repositório:

```bash
python3 -m http.server 5500 --directory frontend
```

- Interface: `http://localhost:5500`
- Catálogo API: `http://localhost:8080/api/produtos`
- Swagger no perfil `dev`: `http://localhost:8080/swagger-ui.html`

O catálogo de exemplo é habilitado em `dev`. A criação inicial de administrador exige `APP_ADMIN_ENABLED=true` e valores próprios de `APP_ADMIN_NAME`, `APP_ADMIN_EMAIL` e `APP_ADMIN_PASSWORD`. O exemplo mantém essa criação desabilitada.

### Docker Compose

Requer Docker com Compose. Na raiz do repositório:

```bash
export LOCAL_DB_PASSWORD='substitua-por-uma-senha-local'
export JWT_SECRET='substitua-por-uma-chave-aleatoria-de-pelo-menos-32-bytes'
docker compose up --build -d postgres api
```

A API fica em `http://localhost:8080`. O Compose usa `prod`, banco persistido em volume e catálogo de exemplo desabilitado; portanto, o banco novo não terá produtos. Sirva o frontend separadamente e configure um administrador explicitamente para cadastrar o catálogo. Os uploads ficam em volume próprio.

Para parar sem apagar os dados:

```bash
docker compose down
```

O RabbitMQ é opcional:

```bash
docker compose --profile messaging up -d rabbitmq
```

Para API executada no host, configure `APP_MESSAGING_ORDERS_ENABLED=true` e as variáveis `RABBITMQ_*`. Para API no Compose, configure também `RABBITMQ_HOST=rabbitmq` no serviço. O painel local fica em `http://localhost:15672`; o Compose usa credenciais locais `guest/guest` e publica o broker apenas em loopback.

### Imagem do backend

```bash
docker build -t dsuplementos-store .
docker run --rm -p 8080:10000 --env-file backend/.env dsuplementos-store
```

O container usa `PORT=10000` por padrão e executa como usuário sem privilégios. O banco informado deve ser acessível de dentro do container; `localhost` dentro dele não aponta para o host.

## Testes e CI

```bash
# Testes unitários e HTTP; compila também as classes de integração
mvn -f backend/pom.xml test

# Verificação completa, incluindo PostgreSQL descartável via Testcontainers
# Requer Docker disponível
mvn -f backend/pom.xml clean verify

# Alternativa explícita sem Docker: não valida a integração com PostgreSQL
mvn -f backend/pom.xml verify -DskipITs
```

Os testes HTTP exercitam o filtro JWT e as permissões reais, com dependências de autenticação simuladas. Os testes de integração `*IT` usam PostgreSQL e as migrations reais para verificar pedidos, rollback e concorrência. Eles não são ignorados automaticamente quando Docker está ausente: a verificação completa deve falhar nesse caso.

O workflow executa a verificação com Java 21, disponibiliza relatórios, constrói a imagem Docker e verifica o catálogo com API e PostgreSQL no Compose. A existência do arquivo não comprova uma execução remota bem-sucedida. Consulte [GitHub Actions](https://github.com/gbrlc/dsuplementos-store/actions) para obter evidências após publicar as mudanças. O workflow não faz deploy.

## Deploy e limites conhecidos

O frontend configura a API externa em `frontend/config.js`. O Dockerfile deste repositório entrega somente o backend. Configure no serviço da API banco, chave JWT, `SPRING_PROFILES_ACTIVE=prod`, origens CORS e persistência dos uploads.

A interface contém catálogo de apresentação como fallback. Ver produtos na página não comprova funcionamento da API ou do banco. Valide separadamente `GET /api/produtos`, autenticação e persistência em ambiente de homologação.

- Não há gateway de pagamento, cálculo logístico externo ou confirmação financeira.
- JWTs já emitidos continuam válidos até expirar, inclusive após logout ou troca de senha; refresh tokens são revogados.
- Publicação RabbitMQ ocorre após commit, sem outbox ou reentrega garantida. Falhas são registradas em log.
- Uploads verificam extensão, MIME declarado e assinatura de formato, mas não fazem análise completa de imagens ou antivírus.
- Uploads usam filesystem; a persistência depende do volume/disco configurado no deploy.
- O catálogo ainda filtra e ordena em memória.
- Os testes não representam cobertura completa nem comprovam disponibilidade contínua do deploy.

## Autor

Gabriel Costa — Desenvolvedor Backend Java

[GitHub](https://github.com/gbrlc) · [LinkedIn](https://www.linkedin.com/in/gabriel-costa-94b95616a)
