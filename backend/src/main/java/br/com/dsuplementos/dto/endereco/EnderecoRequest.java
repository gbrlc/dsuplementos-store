package br.com.dsuplementos.dto.endereco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @Size(max = 60, message = "O apelido pode ter no maximo 60 caracteres.")
        String apelido,

        @NotBlank(message = "Informe o CEP.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Informe um CEP valido.")
        String cep,

        @NotBlank(message = "Informe o logradouro.")
        @Size(max = 160)
        String logradouro,

        @NotBlank(message = "Informe o numero.")
        @Size(max = 20)
        String numero,

        @Size(max = 100)
        String complemento,

        @NotBlank(message = "Informe o bairro.")
        @Size(max = 100)
        String bairro,

        @NotBlank(message = "Informe a cidade.")
        @Size(max = 100)
        String cidade,

        @NotBlank(message = "Informe o estado.")
        @Pattern(regexp = "[A-Za-z]{2}", message = "Informe a UF com duas letras.")
        String estado,

        Boolean principal
) {
}
