package br.com.dsuplementos.security;

import br.com.dsuplementos.config.SecurityConfig;
import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SecurityHttpTest.Rotas.class, properties = "app.cors.allowed-origins=http://localhost:5500")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityHttpTest.Rotas.class})
class SecurityHttpTest {
    @Autowired MockMvc mvc;
    @MockitoBean JwtService jwt;
    @MockitoBean UsuarioRepository usuarios;

    @RestController
    static class Rotas {
        @GetMapping({"/api/admin/probe", "/api/privado", "/api/produtos/probe"})
        String probe() { return "ok"; }
    }

    @Test void catalogoPublico() throws Exception {
        mvc.perform(get("/api/produtos/probe")).andExpect(status().isOk());
    }
    @Test void anonimoRecebe401() throws Exception {
        mvc.perform(get("/api/privado")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACAO_NECESSARIA"));
    }
    @Test void tokenInvalidoRecebe401() throws Exception {
        when(jwt.extrairEmail("invalido")).thenThrow(new io.jsonwebtoken.MalformedJwtException("invalido"));
        mvc.perform(get("/api/privado").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }
    @Test void clienteNaoAcessaAdministracao() throws Exception {
        autenticar(Role.CLIENTE);
        mvc.perform(get("/api/admin/probe").header("Authorization", "Bearer valido"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }
    @Test void administradorAcessaAdministracao() throws Exception {
        autenticar(Role.ADMIN);
        mvc.perform(get("/api/admin/probe").header("Authorization", "Bearer valido"))
                .andExpect(status().isOk());
    }
    @Test void tokenExpiradoRecebe401() throws Exception {
        when(jwt.extrairEmail("expirado")).thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expirado"));
        mvc.perform(get("/api/privado").header("Authorization", "Bearer expirado"))
                .andExpect(status().isUnauthorized());
    }
    private void autenticar(Role role) {
        when(jwt.extrairEmail("valido")).thenReturn("ana@example.com");
        when(usuarios.findByEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(new Usuario("Ana", "ana@example.com", "hash", role)));
    }
}
