package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ProdutoJpaRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByAtivoTrue();

    Optional<Produto> findByIdAndAtivoTrue(Long id);

    List<Produto> findAllByOrderByNomeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select produto from Produto produto where produto.id = :id")
    Optional<Produto> findByIdComBloqueio(@Param("id") Long id);
}
