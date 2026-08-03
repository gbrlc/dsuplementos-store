package br.com.dsuplementos.dto.pedido;

public record EnderecoPedidoResponse(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado
) {
}
