package tech.lucasbortolatto.dscatalog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

// configuração do ResourceServer do checklist do oauth
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    // Objeto que representa o ambiente de execução da aplicação
    // Instanciado para liberar acesso ao H2 console
    @Autowired
    private Environment environment;

    // rotas abertas para todos
    private static final String[] PUBLIC = {"/oauth/token", "/h2-console/**"};

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
        // libera o acesso ao h2 console
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            http.headers().frameOptions().disable();
        }

        http.authorizeRequests() // configura as autorizações por rotas/roles
                .antMatchers(PUBLIC).permitAll() // public liberado para todos, com ou sem roles
                .antMatchers(HttpMethod.GET, OPERATOR_OR_ADMIN).permitAll()  // permite apenas o método GET para todos nessa rota, com ou sem roles
                .antMatchers(OPERATOR_OR_ADMIN).hasAnyRole("OPERATOR", "ADMIN") // permite todos os métodos nessas rotas pra quem tiver essas roles, sem precisar do prefixo do banco "ROLE_"
                .antMatchers(ADMIN).hasRole("ADMIN") // permite todos os métodos nessas rotas para quem tiver essa role
                .anyRequest().authenticated(); // qualquer outra rota, precisa estar autenticado

        // configura o cors com o bean criado abaixo
        http.cors().configurationSource(corsConfigurationSource());
    }

    /*
        Componentes para configuração de CORS no ResourceServer
        MUITA ATENÇÃO AOS IMPORTS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // na linha abaixo ta liberando geral de qualquer lugar fazer request
        corsConfig.setAllowedOriginPatterns(Arrays.asList("*"));
//        corsConfig.setAllowedOriginPatterns(Arrays.asList("https://meudominio.com"));
        corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "PATCH"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> bean
                = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource()));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}
