# Solicitações de pedido por e-mail

## Escopo

O cliente registra uma solicitação autenticada no site. A loja recebe itens, quantidades, valores estimados e nome/e-mail do solicitante, e confirma disponibilidade, entrega e fechamento pelo WhatsApp. O e-mail não comprova venda ou pagamento. Não há envio de confirmação ao cliente nesta implementação; o e-mail dele é usado como `reply_to` para a loja poder responder.

A implementação está preparada, mas o envio real exige configuração de uma conta de envio e de um broker hospedado. Não confundir testes com serviços simulados com entrega na caixa de entrada do comércio.

## Fluxo

1. A transação cria o pedido, limpa o carrinho e grava um snapshot do e-mail em `notificacoes_pedido`.
2. A cada 30 segundos, o dispatcher busca até 20 notificações pendentes com `FOR UPDATE SKIP LOCKED` e publica somente o ID do pedido no RabbitMQ.
3. O consumidor bloqueia a notificação, verifica se já foi enviada e chama a API HTTPS do Resend.
4. O aceite HTTP do provedor preenche `enviado_em`. Isso indica aceite para envio, não entrega comprovada na caixa postal.
5. Falhas HTTP geram até oito tentativas, com intervalos crescentes de um minuto até uma hora. Ao atingir o limite, a linha permanece no banco para análise operacional.

Se o broker falhar, a linha permanece pendente e pode ser publicada novamente após dois minutos. Mensagens técnicas rejeitadas vão para uma fila `.dead`. O agendador depende da API estar em execução: instâncias gratuitas do Render podem dormir, atrasando notificações até a próxima ativação.

O payload é imutável e a chave de idempotência é estável por pedido. Duplicatas já confirmadas são ignoradas pelo consumidor. O Resend guarda chaves de idempotência por 24 horas; não há garantia de exatamente uma entrega em interrupções que ultrapassem essa janela. Ambientes de teste devem usar conta/provedor simulado para não colidir com IDs de produção.

## Configuração no Render

O Render gratuito bloqueia SMTP nas portas 25, 465 e 587. O envio usa HTTPS. Referências: [Render](https://render.com/docs/free), [Resend](https://resend.com/docs/api-reference/emails/send-email), [idempotência](https://resend.com/docs/dashboard/emails/idempotency-keys).

Configure somente no ambiente, sem versionar chaves:

| Variável | Valor |
| --- | --- |
| `APP_MAIL_ENABLED` | `true` para registrar e enviar notificações |
| `APP_MAIL_FROM` | Remetente autorizado/verificado no serviço de e-mail |
| `APP_MAIL_TO` | Endereço da loja que receberá as solicitações |
| `RESEND_API_KEY` | Chave do serviço, cadastrada diretamente no ambiente |
| `APP_MESSAGING_ORDERS_ENABLED` | `true` |
| `RABBITMQ_ADDRESSES` | URL `amqps://usuario:senha@host:5671/vhost` fornecida pelo broker |

O RabbitMQ de produção precisa ser acessível ao Render. O container do computador local não fica acessível automaticamente. Esta mudança não contrata serviços pagos nem cria contas externas. Endereço remetente, destinatário e contas ainda precisam ser definidos pelo responsável pela loja.

O remetente deve obedecer às regras do Resend: produção pode exigir um domínio verificado. Não basta informar um endereço Gmail como `from`. Credenciais ficam somente no provedor/Render.

Com o envio desabilitado, pedidos continuam funcionando, mas não criam notificações retroativas. Habilite e valide com uma solicitação identificada como teste antes de anunciar o envio como operacional. Se a configuração obrigatória estiver ausente com o envio habilitado, a aplicação recusa a inicialização com mensagem de configuração.

## Execução local com RabbitMQ

Preencha as variáveis de banco/JWT descritas no README. Para iniciar API, banco e broker:

```bash
export APP_MESSAGING_ORDERS_ENABLED=true
export APP_MAIL_ENABLED=true
export APP_MAIL_FROM='remetente-verificado@seu-dominio.com'
export APP_MAIL_TO='pedidos@seu-dominio.com'
# Configure RESEND_API_KEY no ambiente, sem compartilhar em mensagens ou Git.
docker compose --profile messaging up --build -d postgres rabbitmq api
```

A fila e a exchange são duráveis. O broker local usa volume próprio. Aguarde o RabbitMQ ficar disponível; a aplicação tenta reconectar e a outbox preserva notificações pendentes.

A fila padrão de e-mail usa uma nova identificação para não conflitar com filas anteriores sem argumentos de dead-letter. Não exclua filas existentes de produção para resolver divergência de declaração.

## Diagnóstico operacional

Executar no banco com acesso administrativo:

```sql
SELECT pedido_id, tentativas, proxima_tentativa, publicado_em, enviado_em
FROM notificacoes_pedido
WHERE enviado_em IS NULL
ORDER BY pedido_id;
```

Não publique o conteúdo de `payload`: contém dados do solicitante. Logs da aplicação registram somente o ID do pedido e número de tentativas, sem chaves, corpo HTTP do provedor ou conteúdo do e-mail.

Antes de reprocessar uma linha com oito falhas, corrija o problema de provedor/remetente e confirme se o e-mail já foi aceito no painel do serviço, especialmente após 24 horas. Não anuncie entrega confirmada com base apenas em `enviado_em`.

## Estoque

O comércio informou que não precisa de controle automático de estoque. Assim, solicitações e cancelamentos não baixam nem devolvem unidades e não são bloqueados pelo saldo numérico legado. Produtos inativos continuam indisponíveis. A disponibilidade física é confirmada no atendimento; reposição frequente não é apresentada como garantia de estoque infinito.

O campo numérico existente é preservado por compatibilidade com o schema e a administração, mas não representa saldo sincronizado com as vendas. A interface do cliente deixa de exibir esse número e não promete reserva.
