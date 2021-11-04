package tech.lucasbortolatto.dscatalog.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.lucasbortolatto.dscatalog.entities.Category;
import tech.lucasbortolatto.dscatalog.entities.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT obj FROM Product obj " + // DISTINCT para que não hajam duplicidades dos produtos com mais de uma cat
            "INNER JOIN obj.categories cats " +
            "WHERE " +
            "(COALESCE(:categories) IS NULL OR cats IN :categories) " + // se a category vier nulo, vai dar true na primeira condição e traz todos, com uma jogadinha pro postgres
            "AND " +
            "(LOWER(obj.name) LIKE LOWER(CONCAT('%', :name, '%')))") //se nao tiver nome dai traz todos automaticamente
    Page<Product> find(List<Category> categories, String name, Pageable pageable);
}
