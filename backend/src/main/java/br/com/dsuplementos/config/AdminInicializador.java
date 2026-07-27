package br.com.dsuplementos.config;

import br.com.dsuplementos.domain.Usuario;
import br.com.dsuplementos.domain.enums.Role;
import br.com.dsuplementos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
public class AdminInicializador {

    @Bean
    CommandLineRunner criarAdministradorInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.enabled}") boolean habilitado,
            @Value("${app.admin.name}") String nome,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String senha
    ) {
        return args -> {
            if (!habilitado) {
                return;
            }
            if (nome.isBlank() || email.isBlank() || senha.length() < 8) {
                throw new IllegalStateException("Preencha APP_ADMIN_NAME, APP_ADMIN_EMAIL e APP_ADMIN_PASSWORD.");
            }

            String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
            if (!usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
                Usuario administrador = new Usuario(
                        nome.trim(),
                        emailNormalizado,
                        passwordEncoder.encode(senha),
                        Role.ADMIN
                );
                usuarioRepository.save(administrador);
            }
        };
    }
}
