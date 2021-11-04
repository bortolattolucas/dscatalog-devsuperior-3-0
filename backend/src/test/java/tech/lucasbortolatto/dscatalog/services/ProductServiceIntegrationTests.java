package tech.lucasbortolatto.dscatalog.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import tech.lucasbortolatto.dscatalog.dto.ProductDTO;
import tech.lucasbortolatto.dscatalog.repositories.ProductRepository;
import tech.lucasbortolatto.dscatalog.services.exceptions.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

// Carrega o contexto inteiro da aplicação para a execução dos testes
// Teste de integração testam todos os componentes de conversando de forma real
// Desde a camada web ateh o banco
@SpringBootTest
// Faz com que o estado de um teste não afete o outro
@Transactional
public class ProductServiceIntegrationTests {

    // Pega o proprio bean do contexto
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private long existingId;
    private long nonExistingId;
    private long countTotalProducts;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }

    @Test
    public void deleteShouldRemoveResourceWhenIdExists() {
        productService.delete(existingId);

        assertEquals(countTotalProducts - 1, productRepository.count());
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () -> productService.delete(nonExistingId));
    }

    @Test
    public void findAllPagedShouldReturnPageWhenPage0Size10() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ProductDTO> result = productService.findAllPaged(0L, "", pageRequest);

        assertFalse(result.isEmpty());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        assertEquals(countTotalProducts, result.getTotalElements());
    }

    @Test
    public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExist() {
        PageRequest pageRequest = PageRequest.of(50, 10);

        Page<ProductDTO> result = productService.findAllPaged(0L, "", pageRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findAllPagedShouldReturnSortedPageWhenSortByName() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name"));

        Page<ProductDTO> result = productService.findAllPaged(0L, "", pageRequest);

        assertFalse(result.isEmpty());
        assertEquals("Macbook Pro", result.getContent().get(0).getName());
        assertEquals("PC Gamer", result.getContent().get(1).getName());
        assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());
    }
}
