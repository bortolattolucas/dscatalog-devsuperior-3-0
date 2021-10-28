package tech.lucasbortolatto.dscatalog.services.validation;

import org.springframework.beans.factory.annotation.Autowired;
import tech.lucasbortolatto.dscatalog.dto.UserInsertDTO;
import tech.lucasbortolatto.dscatalog.repositories.UserRepository;
import tech.lucasbortolatto.dscatalog.resources.exceptions.FieldMessage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

// implementa a interface ConstraintValidator do beans validation com generics usando a anotação personalizada com a classe validada
public class UserInsertValidator implements ConstraintValidator<UserInsertValid, UserInsertDTO> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void initialize(UserInsertValid ann) {
    }

    // nesse método que rola a validação em si
    @Override
    public boolean isValid(UserInsertDTO dto, ConstraintValidatorContext context) {

        // Lista que vai guardar os possíveis erros pegos na validação
        List<FieldMessage> list = new ArrayList<>();

        if (userRepository.findByEmail(dto.getEmail()) != null) {
            list.add(new FieldMessage("email", "Email já existe"));
        }

        // insere os erros de validação na lista de erros do beanValidation pra classe UserInsertDTO
        for (FieldMessage e : list) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
                    .addConstraintViolation();
        }

        return list.isEmpty();
    }
}

