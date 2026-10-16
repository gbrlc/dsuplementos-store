# Verificação das correções — 16/10/2026

## Validação local

- `mvn -f backend/pom.xml clean verify -DskipITs`: sucesso, 28 testes unitários e HTTP, sem falhas ou erros. Classes de integração também compiladas.
- JDK local: 26.0.1; alvo de compilação: Java 21.
- `git diff --check`: sem erros.
- A verificação local completa parou por ausência de Docker. A integração foi posteriormente validada no GitHub Actions, sem ignorar testes.

## GitHub Actions: verificação remota concluída

[Execução na main, commit 2494d06](https://github.com/gbrlc/dsuplementos-store/actions/runs/38940418046): concluída com sucesso.

[Execução anterior à integração da PR](https://github.com/gbrlc/dsuplementos-store/actions/runs/38940124429): concluída com sucesso. Os relatórios XML disponibilizados pelo workflow confirmam 28 testes unitários/HTTP e cinco testes de integração, sem falhas, erros ou testes ignorados.

Etapas aprovadas em Java 21 e ambiente com Docker:

- `mvn clean verify`, incluindo migrations reais e PostgreSQL/Testcontainers;
- testes de reserva/cancelamento repetido, rollback após falha em outro item, cancelamentos concorrentes, disputa pela última unidade e reutilização concorrente de refresh token;
- build da imagem Docker;
- inicialização de API e PostgreSQL pelo Compose e consulta HTTP ao catálogo;
- publicação dos relatórios de teste.

As correções foram publicadas e integradas pela [PR #5](https://github.com/gbrlc/dsuplementos-store/pull/5).

## Render: publicação concluída

O primeiro deploy falhou com erro de conexão PostgreSQL `tenant/user not found`. O painel do Supabase confirmou que o projeto existente estava pausado. Ele foi retomado no plano gratuito e apresentou status `Healthy`, sem criar outro banco ou alterar a senha.

Após a retomada, o redeploy do commit `10f7a00` concluiu com `Deploy succeeded | Live` em 16/10/2026 às 14:44, horário de São Paulo. A imagem inicia com Java 21, perfil `prod` e usuário sem privilégios. O Flyway validou o schema existente.

[Deploy confirmado no Render](https://dashboard.render.com/web/srv-daku00ajnfac73fvbqk0/deploys/dep-db963o7btjjs7399vt20).

Verificações HTTP da API publicada:

| Cenário | Resultado |
| --- | --- |
| `GET /api/produtos` | HTTP 200, lista de 12 produtos |
| `GET /api/pedidos` sem autenticação | HTTP 401, código `AUTENTICACAO_NECESSARIA` |
| `GET /api/admin/produtos` sem autenticação | HTTP 401 |
| `GET /api/pedidos` com JWT inválido | HTTP 401 |
| Preflight CORS do frontend publicado | HTTP 200, origem do frontend autorizada |
| Frontend publicado | HTTP 200 |

Esses resultados são verificações pontuais, não uma garantia de disponibilidade contínua nem um teste completo de vendas autenticadas.

## Supabase: exposição independente da API Java corrigida

O painel apontou tabelas sem RLS. Uma requisição anônima `HEAD` à tabela de usuários, com projeção de ID e limite zero, retornou HTTP 200. Nenhum registro foi obtido nessa verificação.

Como este repositório usa JDBC no backend e não usa SDK, REST ou GraphQL do Supabase, a Data API foi desabilitada no provedor. A configuração salva passou a indicar `Enable Data API: off`, e a consulta externa passou a retornar HTTP 503. As consultas JDBC do backend continuaram funcionando após essa mudança. Não foram alterados registros de clientes ou políticas de cada tabela.

Se a Data API voltar a ser habilitada, será necessário revisar grants e RLS antes de expor as tabelas. [Orientação oficial do Supabase para acesso somente por conexão direta](https://supabase.com/docs/guides/database/secure-data).

## Limites de escopo

Não foram criados pedidos nem alterado estoque de produção para testar a publicação. Concorrência e rollback foram exercitados no PostgreSQL descartável do CI. O mecanismo real de solicitação por e-mail e a política de conciliação com vendas fechadas no WhatsApp ainda precisam ser identificados/definidos com o responsável pelo comércio.
