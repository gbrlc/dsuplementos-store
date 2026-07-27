package br.com.dsuplementos.repository;

import br.com.dsuplementos.domain.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
    Optional<Marca> findByNomeIgnoreCase(String nome);
}
