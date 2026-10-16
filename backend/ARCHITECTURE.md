# Arquitetura do backend DSuplementos

Monólito Spring Boot em camadas. O frontend é servido separadamente e usa HTTP/JSON com JWT para acessar a API. A implementação inicia em `DsuplementosApplication`; `Main`, `model` e `service` preservam uma etapa anterior em Java puro.

## Fluxo principal

```text
Frontend -> SecurityFilterChain -> Controller -> Service transacional -> PostgreSQL
                                                      |
                                                      +-> outbox notificacoes_pedido
                                                               |
                                                          dispatcher agendado
                                                               |
                                                            RabbitMQ
                                                               |
                                                         consumidor -> Resend HTTPS -> loja
```

Controllers definem contratos e validam DTOs. Services aplicam regras e propriedade de recursos. Repositories JPA e JDBC persistem dados. DTOs e MapStruct evitam expor entidades diretamente.

## Negócio

O catálogo gera solicitações de interesse, e a venda é concluída no atendimento pelo WhatsApp. Não há gateway de pagamento. Frete e pagamento presentes no código são simulações.

Conforme orientação do comércio, a aplicação não mantém controle automático de saldo. Criar/cancelar solicitações não altera estoque e saldo numérico não limita itens. Produtos inativos continuam sendo rejeitados. O campo legado de estoque não é apresentado como saldo físico sincronizado.

## Pedido e consistência

A criação bloqueia o carrinho existente, valida o endereço do próprio usuário, rejeita carrinho vazio e produtos inativos, calcula valores no servidor, grava snapshots de itens/endereço e limpa o carrinho na mesma transação.

Quando e-mail está habilitado, a mesma transação grava o payload da notificação na outbox. Falha ao preparar a notificação desfaz também o pedido; o envio externo não ocorre nessa transação. O cancelamento bloqueia o pedido e valida sua transição, sem recompor saldo.

Snapshots preservam nome, preço e endereço daquele instante, mesmo que catálogo ou cadastro sejam alterados depois.

## Notificações

Dispatcher usa `FOR UPDATE SKIP LOCKED` para publicar IDs pendentes no RabbitMQ, sem dados pessoais no broker. O consumidor bloqueia a linha da notificação para evitar processamento concorrente e ignora registros já enviados. O payload é imutável, e a chamada HTTPS usa chave de idempotência por pedido.

Há filas duráveis, dead-letter para rejeições técnicas, republicação de pendências e até oito tentativas de envio com backoff. O aceite pelo provedor não confirma entrega na caixa de entrada. Interrupções superiores à janela de idempotência do provedor podem exigir reconciliação manual. A operação em Render gratuito depende também da instância estar acordada.

A integração fica desabilitada por padrão. Conta de envio, remetente autorizado, destinatário e broker externo devem ser configurados antes de afirmar que e-mails reais estão sendo enviados. Veja [configuração e limites](../docs/email-pedidos.md).

## Segurança

- BCrypt para senhas, JWT de duração limitada e refresh tokens opacos persistidos por SHA-256.
- Renovação e revogação bloqueiam a linha do refresh token; AuthService emite o substituto na transação de renovação.
- Rotas públicas explícitas, perfil `ADMIN` em `/api/admin/**` e verificação de propriedade nos services.
- Logout e troca de senha revogam refresh tokens; JWTs emitidos continuam válidos até expirar.
- Upload verifica extensão, MIME declarado e assinatura binária; não faz análise completa ou antivírus.
- No Supabase de produção, Data API desabilitada; somente backend Java acessa o banco via JDBC. Essa configuração operacional deve ser reproduzida em novos ambientes.
- Logs de e-mail não incluem corpo HTTP do provedor, payload ou chaves.

## Persistência e execução

Flyway aplica migrations de `database/migrations`, empacotadas em `db/migration`. Hibernate usa `ddl-auto: validate`. O perfil `prod` desabilita Swagger, catálogo de exemplo e limpeza do Flyway; configuração sensível entra pelo ambiente.

O Dockerfile entrega somente a API, com Java 21 e usuário sem privilégios. Compose inclui PostgreSQL, API e RabbitMQ opcional com volume persistente. O frontend usa a URL externa configurada em `frontend/config.js`.

## Verificação

JUnit/Mockito cobrem regras isoladas; MockMvc exercita filtro JWT e permissões HTTP. Testcontainers/PostgreSQL verifica migrations, rollback, concorrência e ausência de alterações no saldo legado. A integração de e-mail usa PostgreSQL e RabbitMQ reais em containers, com gateway de e-mail simulado; um servidor HTTP local verifica o contrato do adaptador Resend sem enviar mensagens reais.

`mvn test` executa testes unitários/HTTP. `mvn verify` também exige Docker e executa `*IT`. CI usa Java 21, publica relatórios, constrói a imagem e testa a API com banco no Compose. O [registro anterior de publicação](../docs/verificacao.md) é histórico e não comprova a ativação da nova integração de e-mail.
