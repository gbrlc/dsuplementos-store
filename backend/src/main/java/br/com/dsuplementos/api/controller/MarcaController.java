package br.com.dsuplementos.api.controller;

import br.com.dsuplementos.api.service.ProdutoService;
import br.com.dsuplementos.dto.produto.MarcaResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/marcas")
public class MarcaController {

    private final ProdutoService produtoService;

    public MarcaController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<MarcaResponse> listar() {
        return produtoService.listarMarcas();
    }
}
