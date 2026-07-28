package br.com.dsuplementos.dto.comum;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaginaResponseTest {

    @Test
    void deveConverterPaginaDoSpringParaContratoDaApi() {
        Page<String> pagina = new PageImpl<>(
                List.of("Whey", "Creatina"),
                PageRequest.of(1, 2),
                5
        );

        PaginaResponse<String> resposta = PaginaResponse.de(pagina, String::toUpperCase);

        assertThat(resposta.conteudo()).containsExactly("WHEY", "CREATINA");
        assertThat(resposta.pagina()).isEqualTo(1);
        assertThat(resposta.tamanho()).isEqualTo(2);
        assertThat(resposta.totalElementos()).isEqualTo(5);
        assertThat(resposta.totalPaginas()).isEqualTo(3);
        assertThat(resposta.primeira()).isFalse();
        assertThat(resposta.ultima()).isFalse();
    }
}
