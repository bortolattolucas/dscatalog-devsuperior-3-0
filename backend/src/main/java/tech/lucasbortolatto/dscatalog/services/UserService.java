package tech.lucasbortolatto.dscatalog.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.lucasbortolatto.dscatalog.dto.RoleDTO;
import tech.lucasbortolatto.dscatalog.dto.UserDTO;
import tech.lucasbortolatto.dscatalog.dto.UserInsertDTO;
import tech.lucasbortolatto.dscatalog.dto.UserUpdateDTO;
import tech.lucasbortolatto.dscatalog.entities.Role;
import tech.lucasbortolatto.dscatalog.entities.User;
import tech.lucasbortolatto.dscatalog.repositories.RoleRepository;
import tech.lucasbortolatto.dscatalog.repositories.UserRepository;
import tech.lucasbortolatto.dscatalog.services.exceptions.DatabaseException;
import tech.lucasbortolatto.dscatalog.services.exceptions.ResourceNotFoundException;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

// cria um bean (componente) com instancia gerenciada pelo spring e injeção com autowired
@Service
// esse service é do User e tbm implementa pra ser do UserDetailsService, para o authorization server
public class UserService implements UserDetailsService {

    // bean criado na propria interface repository, assim como esse service
    @Autowired
    UserRepository userRepository;

    // bean criado na propria interface repository, assim como esse service
    @Autowired
    RoleRepository roleRepository;

    // bean criado na config
    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    // instancia estática só pra criar logs no console dos eventos dessa classe
    // criada pra auxiliar pra ver se ta funcionando funcoes do spring security
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        Optional<User> obj = userRepository.findById(id);
        User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new UserDTO(entity); // ja carrega as roles
    }

    @Transactional
    public UserDTO insert(UserInsertDTO dto) {
        User entity = new User();
        copyDtoToEntity(dto, entity);
        // com os dados copiados, por ser um userinsertdto, a senha é passada separadamente abaixo
        // porem ja criptografada pelo passwordEncoder
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity = userRepository.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        try {
            User entity = userRepository.getOne(id);
            copyDtoToEntity(dto, entity);
            entity = userRepository.save(entity);
            return new UserDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Entity not found with id " + id);
        }
    }

    public void delete(Long id) {
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Entity not found with id " + id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation");
        }
    }

    private void copyDtoToEntity(UserDTO dto, User entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());

        entity.getRoles().clear();
        for (RoleDTO roleDTO : dto.getRoles()) {
            Role role = roleRepository.getOne(roleDTO.getId());
            entity.getRoles().add(role);
        }
    }

    // método da interface UserDetailsService para o authorization server
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // como o atributo do username é o email, to chamando o metodo do repository que busca por email
        User user = userRepository.findByEmail(username);

        // se o usuário não foi encontrado, tem que disparar a exception indicada pelo método da interface
        // pra respeitar o processo de autenticacao do spring security
        // e exibir no console com logger pra auxiliar o estudo
        if (user == null) {
            logger.error("User not found: " + username);
            throw new UsernameNotFoundException("Email not found");
        }

        // exibe no console que o usuario foi encontrado pra auxiliar o processo dos estudos
        logger.info("User found: " + username);
        // como a nossa classe User implementa UserDetails, retorna-se o usuario encontrado
        return user;
    }
}
