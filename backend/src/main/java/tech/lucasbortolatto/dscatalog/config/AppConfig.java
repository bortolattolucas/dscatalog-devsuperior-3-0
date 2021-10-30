package tech.lucasbortolatto.dscatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

// classe de configuração da aplicação inteira
@Configuration
public class AppConfig {

    // essa anotacao cria um singleton gerenciado pelo spring
    // da mesma forma que funciona com componentes como services, repositories, etc...
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // beans para usar o JWT
    @Bean
    // sem a anotação abaixo fica dando erro no autowired por estar reconhecendo 2 instancias pro mesmo bean
    // dps da pra entender isso melhor
    @Primary
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
        tokenConverter.setSigningKey("MY-JWT-SECRET"); //hardcoded por enquanto
        return tokenConverter;
    }

    @Bean
    public JwtTokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }
}
