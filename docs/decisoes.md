# Decisões técnicas

## 1. Venda concluída fora do sistema

**Contexto:** pequeno comércio com atendimento humano. O uso informado é catálogo, solicitação por e-mail e fechamento pelo WhatsApp.

**Decisão:** manter pagamento online fora do escopo. Documentar separadamente recursos demonstrativos e operação real. O envio de e-mail não foi identificado no código atual e não é apresentado como uma integração comprovada.

**Consequência:** pedido persistido e estado simulado `PAGO` não são evidência de pagamento. Política de reserva e conciliação com vendas externas requer definição com o comércio.

## 2. Monólito em camadas com PostgreSQL

**Decisão:** controllers para HTTP, services para casos de uso transacionais, repositories para persistência, entidades para estado e DTOs para contratos. Flyway evolui o banco e Hibernate valida o mapeamento.

**Consequência:** execução simples para o porte do negócio, sem necessidade de microserviços. Classes de Java puro preservadas representam histórico de aprendizado e não devem ser confundidas com o caminho da API.

## 3. Estoque e transições serializados

**Decisão:** bloquear o carrinho existente antes de criar pedido e nas alterações de itens; bloquear pedido antes de pagar/cancelar; bloquear produto antes de reservar, devolver ou atualizar estoque administrativo. Adquirir bloqueios de produtos em ordem de ID para reduzir risco de deadlock. Revalidar estado após adquirir o bloqueio.

**Consequência:** operações concorrentes aguardam a transação em curso. Não há reserva no carrinho, somente no pedido. A criação simultânea do primeiro carrinho ainda depende da restrição única por usuário; seu tratamento amigável é uma melhoria pendente. Não há contrato de idempotência HTTP por chave.

## 4. Snapshots de pedido

**Decisão:** copiar nome, preço unitário e endereço ao criar o pedido.

**Consequência:** alterações de catálogo e cadastro não reescrevem o histórico. O servidor calcula os valores usando dados persistidos.

## 5. Sessão com JWT e refresh token opaco

**Decisão:** BCrypt para senhas, JWT de duração limitada e refresh token aleatório persistido apenas como SHA-256. Renovação e revogação bloqueiam a linha do token; a renovação completa revoga o anterior e emite o substituto na transação de AuthService.

**Consequência:** não armazenar o refresh token puro no banco. Logout e troca de senha não invalidam imediatamente JWTs emitidos. O frontend guarda sessão em localStorage, o que exige cuidado com XSS. Corridas envolvendo troca de senha e emissão de sessões ainda não são cobertas pela suíte.

## 6. Mensageria opcional após commit

**Decisão:** não declarar recursos do broker quando a integração está desabilitada. Quando habilitada, enviar evento somente após commit e registrar falha de envio sem responder erro para um pedido já confirmado.

**Consequência:** não existe atomicidade entre banco e broker. Uma interrupção do processo ou indisponibilidade pode perder eventos. Sem consumidor de e-mail neste repositório, não se atribui notificação ao publisher. Outbox só se justifica quando houver requisito real de entrega confiável.

## 7. Verificação em níveis

**Decisão:** testes unitários e MockMvc no ciclo `test`; testes PostgreSQL/Testcontainers no `verify`, com Failsafe. CI executa ambos e constrói a imagem.

**Consequência:** mocks não comprovam concorrência ou migrations; Docker é obrigatório para a verificação completa. `-DskipITs` é uma escolha explícita e não equivale a validar integração.

## 8. Validação de uploads

**Decisão:** exigir extensão e MIME de imagem, verificar assinatura binária de JPEG/PNG/WebP e salvar com UUID em diretório delimitado. Fechar streams de leitura explicitamente.

**Consequência:** conteúdo HTML renomeado como imagem é rejeitado. A assinatura não comprova integridade completa do arquivo; não há decodificação integral nem antivírus.

## 9. Supabase usado como PostgreSQL, com Data API desabilitada

**Contexto:** a aplicação acessa o banco via JDBC; o navegador usa somente a API Java. Durante a publicação foi constatado acesso anônimo pela Data API do provedor a uma tabela sem RLS, usando HEAD com limite zero e sem obter registros.

**Decisão:** desabilitar a Data API no painel do Supabase para impedir acesso externo às tabelas por esse caminho. Preservar a conexão JDBC usada pelo backend.

**Consequência:** a configuração é operacional, específica do provedor; não é aplicada por Flyway. Deve ser verificada ao recriar o ambiente. Antes de habilitar SDK/REST/GraphQL do Supabase, revisar grants e RLS. O plano gratuito também pode pausar o projeto; a retomada do banco deve preceder um redeploy se isso ocorrer.
