package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :pedidoId and p.usuario.id = :usuarioId")
    Optional<Pedido> findByIdAndUsuarioId(@Param("pedidoId") Long pedidoId, @Param("usuarioId") Long usuarioId);
}
