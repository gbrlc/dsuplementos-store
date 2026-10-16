package br.com.dsuplementos.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class NotificacaoPedidoRepository {
    private final JdbcTemplate jdbc;
    public NotificacaoPedidoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void registrar(Long pedidoId, String payload) {
        jdbc.update("insert into notificacoes_pedido(pedido_id, payload) values (?, ?)", pedidoId, payload);
    }
    public List<Long> pendentesParaPublicar() {
        return jdbc.queryForList("""
                select pedido_id from notificacoes_pedido
                 where enviado_em is null and tentativas < 8 and proxima_tentativa <= now()
                   and (publicado_em is null or publicado_em < now() - interval '2 minutes')
                 order by pedido_id limit 20 for update skip locked
                """, Long.class);
    }
    public void publicado(Long pedidoId) {
        jdbc.update("update notificacoes_pedido set publicado_em = now() where pedido_id = ?", pedidoId);
    }
    public Optional<Notificacao> bloquear(Long pedidoId) {
        return jdbc.query("""
                select payload, tentativas, proxima_tentativa, enviado_em
                  from notificacoes_pedido where pedido_id = ? for update
                """, (rs, row) -> new Notificacao(rs.getString(1), rs.getInt(2),
                    rs.getTimestamp(3).toInstant(), rs.getTimestamp(4) == null ? null : rs.getTimestamp(4).toInstant()),
                pedidoId).stream().findFirst();
    }
    public void enviado(Long pedidoId) {
        jdbc.update("update notificacoes_pedido set enviado_em = now() where pedido_id = ?", pedidoId);
    }
    public void falhou(Long pedidoId, int tentativas) {
        long segundos = Math.min(3600, 60L << Math.min(tentativas - 1, 6));
        jdbc.update("""
                update notificacoes_pedido set tentativas = ?,
                       proxima_tentativa = now() + (? * interval '1 second'), publicado_em = null
                 where pedido_id = ?
                """, tentativas, segundos, pedidoId);
    }
    public record Notificacao(String payload, int tentativas, Instant proximaTentativa, Instant enviadoEm) {
        public boolean podeEnviar(Instant agora) {
            return enviadoEm == null && tentativas < 8 && !proximaTentativa.isAfter(agora);
        }
    }
}
