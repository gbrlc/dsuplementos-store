package br.com.dsuplementos.dto.endereco;

public record EnderecoResponse(
        Long id,
        String apelido,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        boolean principal
) {
}
