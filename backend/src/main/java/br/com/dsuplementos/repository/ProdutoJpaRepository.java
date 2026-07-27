package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoJpaRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByAtivoTrue();

    Optional<Produto> findByIdAndAtivoTrue(Long id);

    List<Produto> findAllByOrderByNomeAsc();
}
