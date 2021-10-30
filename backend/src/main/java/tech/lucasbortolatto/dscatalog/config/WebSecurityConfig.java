package tech.lucasbortolatto.dscatalog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// classe que extende a configuração de security da aplicação
// para sobreescrever alguma configuração específica
// inicialmente criada para liberar todos os endpoint no configure ("/**")
// considerando que a dependencia do security ja aplica a segurança na aplicação
// como extende a interface WebSecurityConfigurerAdapter e é a classe de configuração da segurança
// esta sendo usada como parte do checklist das implementacoes do security da aplicacao
// com o checklist implementado, pra liberar todos os endpoint foi alterado o antMatchers ("/actuator/**")
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // bean criado no appConfig
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // implementado pelo nosso UserService, representando essa responsabilidade do checklist
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/actuator/**");
    }

    /*
        Sobreescrita de métodos pra continuar o checklist de implementação do security
        Com essa configuração, vai ser considerado o Usuario e o tipo de criptografia da senha dele
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // informa o service pra buscar o usuario e o tipo de criptografia pra analisar a senha
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    // bean para autenticação, declarado aqui manualmente para aplicar a anotação
    // deixando assim o bean disponivel no contexto da aplicação, pq a instancia vai ser usada
    // durante o processo de autorização
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
