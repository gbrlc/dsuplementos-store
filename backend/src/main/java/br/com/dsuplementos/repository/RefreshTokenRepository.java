package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.RefreshToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken token
               set token.revogadoEm = :agora
             where token.usuario.id = :usuarioId
               and token.revogadoEm is null
            """)
    int revogarAtivosPorUsuarioId(@Param("usuarioId") Long usuarioId, @Param("agora") Instant agora);
}
