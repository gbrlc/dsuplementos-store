package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {
    Optional<Carrinho> findByUsuarioId(Long usuarioId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Carrinho c where c.usuario.id = :usuarioId")
    Optional<Carrinho> findByUsuarioIdComBloqueio(@Param("usuarioId") Long usuarioId);
}
