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

## Render: incidente identificado durante a publicação

O Render iniciou automaticamente o deploy do commit `2494d06`. A imagem foi construída, mas a aplicação falhou ao conectar ao banco com `FATAL: (ENOTFOUND) tenant/user ... not found`.

O painel do Supabase confirmou que o projeto existente `dsuplementos-store` estava pausado. A retomada foi solicitada no plano gratuito, preservando o banco existente. Na elaboração deste registro, o provedor ainda mostrava `Coming up`; não se apresenta esse primeiro deploy como bem-sucedido.

O frontend respondeu HTTP 200. Isso não comprova a API: há catálogo de apresentação como fallback. A validação final deve consultar o catálogo da API, as rotas protegidas e o CORS após o banco voltar e o novo deploy concluir.

## Limites de escopo

Não foram criados pedidos nem alterado estoque de produção para testar a publicação. Concorrência e rollback foram exercitados no PostgreSQL descartável do CI. O mecanismo real de solicitação por e-mail e a política de conciliação com vendas fechadas no WhatsApp ainda precisam ser identificados/definidos com o responsável pelo comércio.
