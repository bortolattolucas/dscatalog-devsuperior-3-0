package tech.lucasbortolatto.dscatalog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.lucasbortolatto.dscatalog.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
