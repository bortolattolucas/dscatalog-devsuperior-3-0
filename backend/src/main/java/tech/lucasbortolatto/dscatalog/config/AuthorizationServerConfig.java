package tech.lucasbortolatto.dscatalog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import tech.lucasbortolatto.dscatalog.components.JwtTokenEnhancer;

import java.util.Arrays;

@Configuration
// Anotação que informa que essa classe de configuração vai representar o Authorization server do checklist
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    // pega o valor da variável de ambiente automaticamente
    @Value("${security.oauth2.client.client-id}")
    private String clientId;

    // pega o valor da variável de ambiente automaticamente
    @Value("${security.oauth2.client.client-secret}")
    private String clientSecret;

    // pega o valor da variável de ambiente automaticamente
    @Value("${jwt.duration}")
    private Integer jwtDuration;

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

    // bean do "aprimorador" do JWT
    @Autowired
    private JwtTokenEnhancer jwtTokenEnhancer;

    // configuração sobre as credenciais do usuário
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
    }

    // configuração sobre as credenciais da aplicação que vai consumir esse backend
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient(clientId)
                .secret(bCryptPasswordEncoder.encode(clientSecret))
                .scopes("read", "write")
                .authorizedGrantTypes("password") // tem varios tipos no oauth2
                .accessTokenValiditySeconds(jwtDuration); //token dura 1 dia por padrao do properties
    }

    // configuração dos endpoints, sobre quem vai autorizar em qual formato de token
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // Cria um objeto de cadeia de aprimoramento do JWT, uninco o converter e o enhancer na cadeia
        TokenEnhancerChain chain = new TokenEnhancerChain();
        chain.setTokenEnhancers(Arrays.asList(jwtAccessTokenConverter, jwtTokenEnhancer));

        endpoints.authenticationManager(authenticationManager)
                .tokenStore(jwtTokenStore)
                .accessTokenConverter(jwtAccessTokenConverter)
                .tokenEnhancer(chain); // seta a cadeia de aprimoramento criada para os JWT usados
    }
}
