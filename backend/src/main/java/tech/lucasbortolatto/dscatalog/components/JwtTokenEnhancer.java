package tech.lucasbortolatto.dscatalog.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;
import tech.lucasbortolatto.dscatalog.entities.User;
import tech.lucasbortolatto.dscatalog.repositories.UserRepository;

import java.util.HashMap;
import java.util.Map;

// Componente auto gerenciado pelo Spring responsável por "aprimorar" o JWT pra ser usado no AuthorizationServerConfig
@Component
public class JwtTokenEnhancer implements TokenEnhancer {

    @Autowired
    private UserRepository userRepository;

    // O método abaixo entra no ciclo de vida do token e altera o mesmo conforme configurado abaixo.
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
        // pega o username configurado no security como parâmetro de busca por e-mail do usuário
        User user = userRepository.findByEmail(oAuth2Authentication.getName());

        // Criação do Map com as propriedades adicionais a irem para o token
        Map<String, Object> map = new HashMap<>();
        map.put("userFirstName", user.getFirstName());
        map.put("userId", user.getId());

        // Downcast para o tipo que permite adicionar as propriedades adicionais
        DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) oAuth2AccessToken;
        token.setAdditionalInformation(map);

        // Retorna o token com as propriedades adicionais inseridas, que retornarão no json de autenticação
        return token;
    }
}
