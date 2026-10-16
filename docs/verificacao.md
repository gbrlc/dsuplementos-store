# Verificação das correções — 16/10/2026

## Executado localmente

- `mvn -f backend/pom.xml clean verify -DskipITs`: sucesso, 28 testes unitários e HTTP, sem falhas ou erros. Classes de integração também compiladas.
- JDK local: 26.0.1; alvo de compilação: Java 21. CI configurado para executar em Java 21; essa execução remota ainda não foi comprovada.
- `git diff --check`: sem erros após as correções.
- Verificação completa com `mvn -f backend/pom.xml verify`: interrompida na inicialização do Testcontainers por ausência de ambiente Docker. Não é evidência de aprovação dos testes PostgreSQL.

## Preparado, mas sem execução comprovada

- Cinco testes de integração em `PedidoIT`: reserva/cancelamento repetido, rollback após falha em outro item, cancelamentos concorrentes, disputa pela última unidade e reutilização concorrente de refresh token.
- Build e execução da imagem Docker, volumes e smoke test do Compose.
- Workflow GitHub Actions atualizado; publicação e execução remota não realizadas nesta etapa.

## Situação externa observada no diagnóstico

- Frontend publicado respondeu HTTP 200.
- Consulta ao catálogo da API excedeu 20 segundos; resultado inconclusivo.
- Consulta pública a execuções de Actions retornou zero execuções naquele momento.
- Essas observações não comprovam disponibilidade contínua nem a versão implantada.

## Pendências operacionais

Identificar o mecanismo real de solicitação por e-mail; definir reserva e conciliação de estoque com vendas fechadas no WhatsApp; executar a verificação completa em ambiente com Docker e conferir os relatórios; validar perfil, CORS, banco e uploads no deploy antes de afirmar funcionamento integral.
