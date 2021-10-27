package tech.lucasbortolatto.dscatalog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

// classe que extende a configuração de security da aplicação
// para sobreescrever alguma configuração específica
// inicialmente criada para liberar todos os endpoint no configure ("/**")
// considerando que a dependencia do security ja aplica a segurança na aplicação
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/**");
    }
}
