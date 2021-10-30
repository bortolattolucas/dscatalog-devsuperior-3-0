package tech.lucasbortolatto.dscatalog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
// Anotação que informa que essa classe de configuração vai representar o Authorization server do checklist
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    // bean do AppConfig
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // bean do AppConfig
    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    // bean do AppConfig
    @Autowired
    private JwtTokenStore jwtTokenStore;

    // bean do WebSecurityConfig
    @Autowired
    private AuthenticationManager authenticationManager;

    // configuração sobre as credenciais do usuário
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
    }

    // configuração sobre as credenciais da aplicação que vai consumir esse backend
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("dscatalog")
                .secret(bCryptPasswordEncoder.encode("dscatalog123"))
                .scopes("read", "write")
                .authorizedGrantTypes("password") // tem varios tipos no oauth2
                .accessTokenValiditySeconds(86400); //token dura 1 dia
    }

    // configuração dos endpoints, sobre quem vai autorizar em qual formato de token
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .tokenStore(jwtTokenStore)
                .accessTokenConverter(jwtAccessTokenConverter);
    }
}
