package br.com.dsuplementos.config;

import br.com.dsuplementos.domain.Marca;
import br.com.dsuplementos.domain.Produto;
import br.com.dsuplementos.domain.enums.Categoria;
import br.com.dsuplementos.repository.MarcaRepository;
import br.com.dsuplementos.repository.ProdutoJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class CatalogoInicializador {

    @Bean
    CommandLineRunner carregarCatalogoInicial(
            ProdutoJpaRepository produtoRepository,
            MarcaRepository marcaRepository,
            @Value("${app.seed-catalog.enabled}") boolean habilitado
    ) {
        return args -> {
            if (!habilitado || produtoRepository.count() > 0) {
                return;
            }

            Marca integralmedica = marca(marcaRepository, "Integralmedica");
            Marca absolut = marca(marcaRepository, "Absolut Nutrition");
            Marca darkLab = marca(marcaRepository, "Dark Lab");
            Marca maxTitanium = marca(marcaRepository, "Max Titanium");

            produtoRepository.save(new Produto("Whey Protein", Categoria.WHEY, integralmedica,
                    new BigDecimal("119.90"), 20, "Whey protein para a rotina diaria de treino.",
                    "assets/products/wheyintegral.jpeg"));
            produtoRepository.save(new Produto("Creatina", Categoria.CREATINA, absolut,
                    new BigDecimal("89.90"), 15, "Creatina monohidratada para treinos intensos.",
                    "assets/products/creatinaabsolut.jpeg"));
            produtoRepository.save(new Produto("Bone Crusher", Categoria.COLAGENO, darkLab,
                    new BigDecimal("89.90"), 10, "Suplemento para suporte articular.",
                    "assets/products/colageno2.jpeg"));
            produtoRepository.save(new Produto("Colageno", Categoria.COLAGENO, maxTitanium,
                    new BigDecimal("89.90"), 12, "Colageno em po para complementar a rotina.",
                    "assets/products/colageno.jpeg"));
            produtoRepository.save(new Produto("Creatina Black", Categoria.CREATINA, darkLab,
                    new BigDecimal("89.90"), 18, "Creatina para uso diario.",
                    "assets/products/creatina3.jpeg"));
            produtoRepository.save(new Produto("Creatina Mono", Categoria.CREATINA, absolut,
                    new BigDecimal("89.90"), 16, "Creatina monohidratada em po.",
                    "assets/products/creatina.jpeg"));
            produtoRepository.save(new Produto("Multi", Categoria.VITAMINAS, integralmedica,
                    new BigDecimal("89.90"), 14, "Multivitaminico para complementar a alimentacao.",
                    "assets/products/multi.jpeg"));
            produtoRepository.save(new Produto("Whey Integral", Categoria.WHEY, integralmedica,
                    new BigDecimal("89.90"), 21, "Whey protein com sabor equilibrado.",
                    "assets/products/wheyintegral.jpeg"));
            produtoRepository.save(new Produto("Whey Max", Categoria.WHEY, maxTitanium,
                    new BigDecimal("89.90"), 17, "Whey protein para o pos-treino.",
                    "assets/products/wheymax.jpeg"));
            produtoRepository.save(new Produto("Whey Zeo", Categoria.WHEY, maxTitanium,
                    new BigDecimal("89.90"), 13, "Whey protein para sua rotina de performance.",
                    "assets/products/wheyy.jpeg"));
            produtoRepository.save(new Produto("Whey Black", Categoria.WHEY, darkLab,
                    new BigDecimal("79.90"), 23, "Whey protein com otimo custo-beneficio.",
                    "assets/products/wheyblack.jpeg"));
            produtoRepository.save(new Produto("Colageno EPA", Categoria.COLAGENO, maxTitanium,
                    new BigDecimal("79.90"), 11, "Colageno para apoiar sua rotina de bem-estar.",
                    "assets/products/colagenoepa.jpeg"));
        };
    }

    private Marca marca(MarcaRepository marcaRepository, String nome) {
        return marcaRepository.findByNomeIgnoreCase(nome)
                .orElseGet(() -> marcaRepository.save(new Marca(nome)));
    }
}
