package tech.lucasbortolatto.dscatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// classe de configuração da aplicação inteira
@Configuration
public class AppConfig {

    // essa anotacao cria um singleton gerenciado pelo spring
    // da mesma forma que funciona com componentes como services, repositories, etc...
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
