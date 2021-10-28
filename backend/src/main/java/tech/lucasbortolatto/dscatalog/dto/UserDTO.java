package tech.lucasbortolatto.dscatalog.dto;

import tech.lucasbortolatto.dscatalog.entities.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    // Anotações de validação, disponibilizadas pela dependência spring-boot-starter-validation
    // são da biblioteca javax, usamos no DTO porque é ele que vai vir da camada web com as infos do usuário
    // https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/overview-summary.html
    // pra ver as disponiveis da pra digitar em cima da classe import javax.validation.constraints. e ctrl + espaço, ir lendo
    // pra essas validações serem usadas de fato nas requests, tem que anotar o objeto parâmetro do método do controller c/ @Valid
    @NotBlank(message = "Campo obrigatório")
    private String firstName;
    private String lastName;

    @Email(message = "E-mail inválido")
    private String email;

    Set<RoleDTO> roles = new HashSet<>();

    public UserDTO() {
    }

    public UserDTO(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public UserDTO(User entity) {
        id = entity.getId();
        firstName = entity.getFirstName();
        lastName = entity.getLastName();
        email = entity.getEmail();
        entity.getRoles().forEach(role -> roles.add(new RoleDTO(role)));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<RoleDTO> getRoles() {
        return roles;
    }
}
