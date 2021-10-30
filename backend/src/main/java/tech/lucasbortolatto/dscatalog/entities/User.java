package tech.lucasbortolatto.dscatalog.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "tb_user")
public class User implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    // para não duplicar o e-mail
    @Column(unique = true)
    private String email;
    private String password;

    // Como existem poucos ROLES, nao vai ter problema carregar junto com o user
    // E isso vai ajudar muito ao autenticar
    // Entao, o manytomany vai ser eager aqui, pra ja carregar suas roles ao carregar o user
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tb_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    public User(Long id, String firstName, String lastName, String email, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /*
        Métodos referentes à interface UserDetails, necessária pro AuthorizationServer
        Coloquei aqui embaixo pra facilitar a separação durante o aprendizado
        Mas poderia ser na ordem normal dos métodos da classe
     */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // pega as Roles do usuário do Set e converte pra classe q implementa o GrantedAuthority
        // pq na visão da interface vão ser os papéis, o atributo Authority da classe Role
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());
    }

    // o método abaixo ja existia, entao removi a implementação anterior e inseri o override
    // pra simbolizar a implementacao da classe de autorização
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // considerando que o username do usuário vai ser o atributo email dessa classe
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
