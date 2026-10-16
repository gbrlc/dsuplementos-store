package br.com.dsuplementos.api.service;

import br.com.dsuplementos.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.Path;
import java.nio.file.Files;
import static org.assertj.core.api.Assertions.*;

class ArquivoServiceTest {
    @TempDir Path pasta;
    @Test void rejeitaHtmlDisfarcadoDeImagem() throws Exception {
        var arquivo = new MockMultipartFile("foto", "foto.png", "image/png", "<html>conteudo</html>".getBytes());
        assertThatThrownBy(() -> new ArquivoService(pasta.toString()).salvarFotoAvaliacao(arquivo))
                .isInstanceOf(RegraDeNegocioException.class);
        try (var arquivos = Files.list(pasta)) { assertThat(arquivos.count()).isZero(); }
    }
    @Test void salvaPngComNomeGerado() throws Exception {
        var imagem = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(imagem, "png", bytes);
        var arquivo = new MockMultipartFile("foto", "../../original.png", "image/png", bytes.toByteArray());
        String url = new ArquivoService(pasta.toString()).salvarFotoAvaliacao(arquivo);
        assertThat(url).startsWith("/uploads/reviews/").doesNotContain("original", "..");
        assertThat(Files.readAllBytes(pasta.resolve("reviews").resolve(url.substring(url.lastIndexOf('/') + 1))))
                .isEqualTo(bytes.toByteArray());
    }
}
