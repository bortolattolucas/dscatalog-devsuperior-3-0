package tech.lucasbortolatto.dscatalog.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.lucasbortolatto.dscatalog.dto.CategoryDTO;
import tech.lucasbortolatto.dscatalog.entities.Category;
import tech.lucasbortolatto.dscatalog.repositories.CategoryRepository;
import tech.lucasbortolatto.dscatalog.services.exceptions.ResourceNotFoundException;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream().map(CategoryDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Optional<Category> obj = categoryRepository.findById(id);
        Category entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new CategoryDTO(entity);
    }

    @Transactional
    public CategoryDTO insert(CategoryDTO dto) {
        Category entity = new Category();
        entity.setName(dto.getName());
        entity = categoryRepository.save(entity);
        return new CategoryDTO(entity);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        try {
            //getOne não cria uma consulta adicional ao banco, ao invés disso apenas cria um objeto provisório p/ transação
            Category entity = categoryRepository.getOne(id);
            entity.setName(dto.getName());
            entity = categoryRepository.save(entity);
            return new CategoryDTO(entity);
        } catch (EntityNotFoundException e) {
            //caso o getOne dentro da transação não encontrar o recurso ele dispara essa EntityNotFoundException
            throw new ResourceNotFoundException("Entity not found with id " + id);
        }
    }
}
