package tech.lucasbortolatto.dscatalog.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.lucasbortolatto.dscatalog.dto.CategoryDTO;
import tech.lucasbortolatto.dscatalog.dto.ProductDTO;
import tech.lucasbortolatto.dscatalog.entities.Category;
import tech.lucasbortolatto.dscatalog.entities.Product;
import tech.lucasbortolatto.dscatalog.repositories.CategoryRepository;
import tech.lucasbortolatto.dscatalog.repositories.ProductRepository;
import tech.lucasbortolatto.dscatalog.services.exceptions.DatabaseException;
import tech.lucasbortolatto.dscatalog.services.exceptions.ResourceNotFoundException;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Long categoryId, String name, Pageable pageable) {
        // se não foi passado categoria, e o valor é 0 consequentemente, o objeto abaixo fica nulo
        List<Category> categories = (categoryId == 0) ? null : List.of(categoryRepository.getOne(categoryId));
        // faz a consulta da página
        Page<Product> page = productRepository.find(categories, name, pageable);
        // faz uma consulta pra evitar o N+1, mesmo que nao atribuindo a ngm
        // só pra manter as relações mapeadas em memoria e nao consultar de novo ao rodar o getCategories()
        productRepository.findProductsWithCategories(page.getContent());
        // retorna a pagina com as categorias, sem dar o problema de N+1 pq os relacionamentos ja estao mapeados
        // obs: o N+1 faria uma consulta adicional pra cada produto encontrado pra buscar suas categorias
        // e dessa forma é feita somente uma consulta a mais pra evitar todas essas possiveis consultas adicionais
        return page.map(x -> new ProductDTO(x, x.getCategories()));
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Optional<Product> obj = productRepository.findById(id);
        Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();
        copyDtoToEntity(dto, entity);
        entity = productRepository.save(entity);
        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            Product entity = productRepository.getOne(id);
            copyDtoToEntity(dto, entity);
            entity = productRepository.save(entity);
            return new ProductDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Entity not found with id " + id);
        }
    }

    public void delete(Long id) {
        try {
            productRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Entity not found with id " + id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation");
        }
    }

    private void copyDtoToEntity(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDate(dto.getDate());
        entity.setImgUrl(dto.getImgUrl());
        entity.setPrice(dto.getPrice());

        entity.getCategories().clear();
        for (CategoryDTO categoryDTO : dto.getCategories()) {
            Category category = categoryRepository.getOne(categoryDTO.getId());
            entity.getCategories().add(category);
        }
    }
}
