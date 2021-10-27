package tech.lucasbortolatto.dscatalog.dto;

// Dto proprio para o insert de user, unico caso onde vai trafegar a senha
// Por extender UserDTO não precisa implementar o serializable de novo
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
