package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByProdutoIdOrderByCriadoEmDesc(Long produtoId);

    Optional<Avaliacao> findByProdutoIdAndUsuarioId(Long produtoId, Long usuarioId);

    @Query("""
            select avaliacao.produto.id, avg(avaliacao.nota), count(avaliacao)
            from Avaliacao avaliacao
            where avaliacao.produto.id in :produtoIds
            group by avaliacao.produto.id
            """)
    List<Object[]> resumirPorProdutoIds(@Param("produtoIds") List<Long> produtoIds);
}
