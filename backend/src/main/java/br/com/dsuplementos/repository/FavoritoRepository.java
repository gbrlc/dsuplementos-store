package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    Optional<Favorito> findByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);

    boolean existsByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);
}
