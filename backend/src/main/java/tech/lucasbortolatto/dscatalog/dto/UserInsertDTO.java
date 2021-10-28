package tech.lucasbortolatto.dscatalog.dto;

import tech.lucasbortolatto.dscatalog.services.validation.UserInsertValid;

// Dto proprio para o insert de user, unico caso onde vai trafegar a senha
// Por extender UserDTO não precisa implementar o serializable de novo
@UserInsertValid // anotacao personalizada para validar
public class UserInsertDTO extends UserDTO {

    private String password;

    public UserInsertDTO() {
        super();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
