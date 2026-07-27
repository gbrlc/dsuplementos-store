package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByProdutoIdOrderByCriadoEmDesc(Long produtoId);

    Optional<Avaliacao> findByProdutoIdAndUsuarioId(Long produtoId, Long usuarioId);
}
