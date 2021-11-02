package tech.lucasbortolatto.dscatalog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

// configuração do ResourceServer do checklist do oauth
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    // rotas abertas para todos
    private static final String[] PUBLIC = {"/oauth/token"};

    // rotas abertas para operadores e administradores
    private static final String[] OPERATOR_OR_ADMIN = {"/products/**", "/categories/**"};

    // rotas abertas apenas para os administradores
    private static final String[] ADMIN = {"/users/**"};

    @Autowired
    private JwtTokenStore jwtTokenStore;

    // com esse método, o resourceServer vai ser capaz de analisar o JWT inteiramente
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(jwtTokenStore);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests() // configura as autorizações por rotas/roles
                .antMatchers(PUBLIC).permitAll() // public liberado para todos, com ou sem roles
                .antMatchers(HttpMethod.GET, OPERATOR_OR_ADMIN).permitAll()  // permite apenas o método GET para todos nessa rota, com ou sem roles
                .antMatchers(OPERATOR_OR_ADMIN).hasAnyRole("OPERATOR", "ADMIN") // permite todos os métodos nessas rotas pra quem tiver essas roles, sem precisar do prefixo do banco "ROLE_"
                .antMatchers(ADMIN).hasRole("ADMIN") // permite todos os métodos nessas rotas para quem tiver essa role
                .anyRequest().authenticated(); // qualquer outra rota, precisa estar autenticado
    }
}
