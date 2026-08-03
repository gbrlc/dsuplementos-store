package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    Optional<Pedido> findByIdAndUsuarioId(Long pedidoId, Long usuarioId);
}
