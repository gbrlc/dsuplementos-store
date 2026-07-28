package br.com.dsuplementos.mapper;

import br.com.dsuplementos.domain.Marca;
import br.com.dsuplementos.domain.Produto;
import br.com.dsuplementos.dto.produto.MarcaResponse;
import br.com.dsuplementos.dto.produto.ProdutoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProdutoMapper {

    MarcaResponse toResponse(Marca marca);

    @Mapping(target = "marcaId", source = "produto.marca.id")
    @Mapping(target = "marca", source = "produto.marca.nome")
    @Mapping(target = "mediaAvaliacoes", source = "mediaAvaliacoes")
    @Mapping(target = "quantidadeAvaliacoes", source = "quantidadeAvaliacoes")
    ProdutoResponse toResponse(Produto produto, double mediaAvaliacoes, long quantidadeAvaliacoes);
}
