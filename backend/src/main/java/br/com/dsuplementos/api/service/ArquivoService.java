package br.com.dsuplementos.api.service;

import br.com.dsuplementos.exception.RegraDeNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ArquivoService {

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");
    private final Path uploadDirectory;

    public ArquivoService(@Value("${app.upload.directory}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String salvarFotoAvaliacao(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new RegraDeNegocioException("Envie uma imagem valida.");
        }
        if (arquivo.getContentType() == null || !arquivo.getContentType().startsWith("image/")) {
            throw new RegraDeNegocioException("Apenas imagens podem ser enviadas na avaliacao.");
        }

        String extensao = extensao(arquivo.getOriginalFilename());
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new RegraDeNegocioException("Use imagens JPG, PNG ou WEBP.");
        }

        try {
            Path reviews = uploadDirectory.resolve("reviews");
            Files.createDirectories(reviews);
            String nomeGerado = UUID.randomUUID() + "." + extensao;
            Path destino = reviews.resolve(nomeGerado).normalize();

            if (!destino.startsWith(reviews)) {
                throw new RegraDeNegocioException("Nome de arquivo invalido.");
            }

            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/reviews/" + nomeGerado;
        } catch (IOException exception) {
            throw new RegraDeNegocioException("Nao foi possivel salvar a imagem da avaliacao.");
        }
    }

    private String extensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return "";
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
