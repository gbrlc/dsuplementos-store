# Arquitetura do backend DSuplementos

Este documento descreve como a API é organizada, quais invariantes protege e onde estão seus limites operacionais. A implementação principal inicia em `DsuplementosApplication.java`; as classes Java sem Spring preservadas no repositório representam uma etapa anterior do projeto e não participam da API publicada.

## Componentes

```text
HTTP request
    │
    ▼
SecurityFilterChain
    │ JWT válido e autorização
    ▼
Controller
    │ DTO validado
    ▼
Service transacional
    │ regra de negócio
    ▼
Spring Data Repository
    │
    ▼
PostgreSQL
```

### Controllers

Os controllers definem as rotas de autenticação, usuário, produtos, marcas, carrinho, favoritos, avaliações, pedidos e administração. Eles recebem DTOs, aplicam validação estrutural e delegam o caso de uso. Regras que dependem do estado do domínio permanecem nos services.

### Services

Os services orquestram repositories e aplicam regras como:

- verificar propriedade do endereço, pedido, favorito ou avaliação;
- calcular valores a partir dos produtos persistidos;
- reservar e recompor estoque;
- validar transições de status do pedido;
- rotacionar e revogar refresh tokens;
- transformar entidades em respostas estáveis da API.

### Repositories

Os repositories usam Spring Data JPA. Consultas específicas carregam agregados necessários aos casos de uso e evitam transferir decisões de negócio para os controllers.

### DTOs e mapeamento

Requests e responses são contratos separados das entidades. Isso impede acoplamento direto do JSON ao modelo persistido, limita campos alteráveis pelo cliente e mantém liberdade para evoluir banco e API de forma independente.

## Autenticação e autorização

1. No cadastro, a senha é codificada com BCrypt.
2. No login, credenciais válidas geram um JWT e um refresh token opaco.
3. O hash do refresh token é persistido; o valor original só é devolvido ao cliente.
4. O filtro JWT lê `Authorization: Bearer <token>` e popula o contexto de segurança.
5. A `SecurityFilterChain` libera somente rotas públicas explícitas.
6. `/api/admin/**` exige o perfil `ADMIN`; demais operações privadas exigem autenticação.
7. Services ainda verificam autorização contextual, como a propriedade de um recurso.

A aplicação não mantém sessão HTTP no servidor. Logout, rotação e troca de senha atuam sobre refresh tokens persistidos; JWTs já emitidos permanecem válidos apenas até sua expiração.

## Criação de pedido

O checkout executa em uma transação:

1. identifica o usuário autenticado;
2. carrega carrinho e endereço pertencentes ao usuário;
3. rejeita carrinho vazio;
4. consulta produtos e valida estoque;
5. calcula preços e total usando dados persistidos;
6. reduz estoque e cria snapshots dos itens;
7. copia o endereço de entrega para o pedido;
8. persiste o pedido e limpa o carrinho;
9. registra o envio do evento para depois do commit, quando RabbitMQ está habilitado.

O cliente não controla o preço final nem a disponibilidade considerada pelo backend.

## Cancelamento e estoque

O cancelamento é permitido apenas para estados previstos pela regra de negócio. Quando aceito, devolve ao estoque as quantidades registradas nos itens e atualiza o status do pedido na mesma fronteira transacional.

## Persistência

O Flyway aplica os scripts de `database/migrations` na inicialização. O Hibernate usa `ddl-auto: validate`, portanto uma divergência entre entidades e schema interrompe a subida em vez de modificar o banco automaticamente.

As migrations incluem estrutura inicial, refresh tokens, auditoria, índices, snapshot de entrega e favoritos. Em produção, a limpeza do Flyway permanece desabilitada.

## Tratamento de erros

`ApiExceptionHandler` converte falhas de validação, autenticação, autorização, recursos ausentes e regras de negócio em um contrato JSON consistente. O cliente recebe código, mensagem, status, caminho e detalhes de campos quando aplicável, sem stack trace.

## Mensageria

O publisher RabbitMQ usa exchange, fila e routing key configuráveis. A integração é desabilitada por padrão e não muda o contrato HTTP do checkout.

O modelo atual envia após commit, no mesmo processo. Falhas AMQP são registradas em log, sem desfazer um pedido já confirmado. Ainda não há garantia atômica entre commit do banco e envio ao broker, nem reentrega automática. Um transactional outbox é a evolução indicada quando a perda eventual de evento deixar de ser aceitável.

## Perfis de execução

- **dev:** SQL visível, catálogo inicial habilitado e Swagger disponível.
- **prod:** catálogo de exemplo desabilitado, Swagger desativado e Flyway clean bloqueado.

Configurações sensíveis entram por variáveis de ambiente e não devem ser versionadas.

## Estratégia de testes

A suíte usa JUnit 5 e Mockito para testar regras isoladas de pedido, usuário, favoritos e refresh token, além do contrato de paginação. O pipeline executa `mvn clean verify`.

A suíte inclui MockMvc com a configuração real de segurança e testes PostgreSQL/Testcontainers (`*IT`) para pedidos, rollback e concorrência. `mvn test` executa testes unitários e HTTP; `mvn verify` também exige Docker e executa integração. A existência dos testes não substitui os relatórios de execução.

## Negócio e decisões

O uso comercial informado é catálogo → solicitação por e-mail → fechamento pelo WhatsApp. O mecanismo de e-mail não foi identificado neste repositório. Pagamento e frete são simulações; pedido criado não confirma uma venda externa. A API e o frontend são entregues separadamente; o Dockerfile inclui apenas o backend.

Consulte [decisões técnicas](../docs/decisoes.md) para bloqueios, reservas, sessão, mensageria e limitações. Pedidos e refresh tokens são carregados com bloqueio de escrita para as operações de transição; alterações administrativas de produtos usam o mesmo bloqueio do estoque.
