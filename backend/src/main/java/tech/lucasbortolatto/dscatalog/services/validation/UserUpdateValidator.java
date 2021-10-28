package tech.lucasbortolatto.dscatalog.services.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;
import tech.lucasbortolatto.dscatalog.dto.UserUpdateDTO;
import tech.lucasbortolatto.dscatalog.entities.User;
import tech.lucasbortolatto.dscatalog.repositories.UserRepository;
import tech.lucasbortolatto.dscatalog.resources.exceptions.FieldMessage;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void initialize(UserUpdateValid ann) {
    }

    @Override
    public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {

        // remove o warning do type safe (por conta do cast explícito do Map)
        @SuppressWarnings("unchecked")
        // pega um Map com os atributos da URL da request
        var uriVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        // extrai o ID passado na URI pra garantir que está considerando o recurso correto
        long userId = Long.parseLong(uriVars.get("id"));

        List<FieldMessage> list = new ArrayList<>();

        // essa validação, junto com a criação de um DTO pra insert e outro pra update,
        // servem pro caso de inserir um novo usuario com e-mail ja existente
        // e tbm quando atualizar um usuario sem mudar o e-mail dele nao dar problema de ja existente
        // mas se tentar com o de outro, ai sim dar o erro de e-mail ja existente para outro usuario
        User user = userRepository.findByEmail(dto.getEmail());
        if (user != null && userId != user.getId()) {
            list.add(new FieldMessage("email", "Email já existe para outro usuário"));
        }

        for (FieldMessage e : list) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
                    .addConstraintViolation();
        }

        return list.isEmpty();
    }
}

