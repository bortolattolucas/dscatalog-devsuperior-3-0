package tech.lucasbortolatto.dscatalog.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.lucasbortolatto.dscatalog.dto.ProductDTO;
import tech.lucasbortolatto.dscatalog.entities.Category;
import tech.lucasbortolatto.dscatalog.entities.Product;
import tech.lucasbortolatto.dscatalog.repositories.CategoryRepository;
import tech.lucasbortolatto.dscatalog.repositories.ProductRepository;
import tech.lucasbortolatto.dscatalog.services.exceptions.DatabaseException;
import tech.lucasbortolatto.dscatalog.services.exceptions.ResourceNotFoundException;
import tech.lucasbortolatto.dscatalog.tests.Factory;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

// Anotação de classe para testes unitários
// Não carrega contexto algum, tudo deve ser mockado e os testes isolados
// Na arquitetura só se leva em consideração as chamadas e os retornos esperados do mock
// Ver as coisas da perspectiva da "visão" do ProductService somente
@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    // creates an instance of the class and injects the mocks that are created with the @Mock (or @Spy) annotations into this instance
    // https://stackoverflow.com/questions/16467685/difference-between-mock-and-injectmocks
    // ou seja, esse objeto que vai ser nosso alvo dos testes unitários recebendo mocks em suas injeções de dependência
    @InjectMocks
    private ProductService productService;

    // Anotação usada para mocks em testes que não sobem contexto da aplicação
    // como é o caso de testes unitários
    // Em casos que a classe de teste sobe o contexto da aplicação, usa-se MockBean
    // Mais detalhes no material de estudo do bootcamp
    @Mock
    private ProductRepository productRepositoryMock;

    @Mock
    private CategoryRepository categoryRepository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private Product product;
    private ProductDTO productDTO;
    private Category category;
    private PageImpl<Product> page;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        product = Factory.createProduct();
        productDTO = Factory.createProductDTO();
        category = Factory.createCategory();
        page = new PageImpl<>(List.of(product));

        // Como sabemos, quando o repository deleta com sucesso, o mesmo não retorna nem dispara nada.
        // Logo, todos os comportamentos devem ser previstos e declarados em seus determinados cenários.
        Mockito.doNothing().when(productRepositoryMock).deleteById(existingId);

        // Comportamento esperado ao tentar excluir um id que não existe, referenciando a mesma variável que vai ser usada nesses pontos
        Mockito.doThrow(EmptyResultDataAccessException.class).when(productRepositoryMock).deleteById(nonExistingId);

        // Comportamento esperado ao tentar excluir um registro com dependentes
        Mockito.doThrow(DataIntegrityViolationException.class).when(productRepositoryMock).deleteById(dependentId);

        // Conforme visto acima, quando o método não retorna nada, o when vem depois
        // Porém, como pode-se ver abaixo, o when vem primeiro quando o método retorna algo

        // Abaixo, ao simular o retorno do findAll, deseja-se simular passando qualquer argumento pra ele
        // Porém, como ele permite várias classes diferentes em suas sobrecargas, é necessário explicitar o tipo
        // Do objeto passado, mesmo que qualquer um objeto
        Mockito.when(productRepositoryMock.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);

        Mockito.when(productRepositoryMock.save(ArgumentMatchers.any())).thenReturn(product);

        Mockito.when(productRepositoryMock.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(productRepositoryMock.findById(nonExistingId)).thenReturn(Optional.empty());

        Mockito.when(productRepositoryMock.find(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(page);

        Mockito.when(productRepositoryMock.getOne(existingId)).thenReturn(product);
        Mockito.when(productRepositoryMock.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);

        Mockito.when(categoryRepository.getOne(existingId)).thenReturn(category);
        Mockito.when(categoryRepository.getOne(nonExistingId)).thenThrow(EntityNotFoundException.class);
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists() {
        ProductDTO result = productService.findById(existingId);

        Assertions.assertNotNull(result);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.findById(nonExistingId));
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() {
        ProductDTO result = productService.update(existingId, productDTO);

        Assertions.assertNotNull(result);
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.update(nonExistingId, productDTO));
    }

    @Test
    public void findAllPagedShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductDTO> result = productService.findAllPaged(0L, "", pageable);

        Assertions.assertNotNull(result);
        Mockito.verify(productRepositoryMock, Mockito.times(1)).find(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        // Não estão sendo usados os import para os métodos estáticos para detalhar melhor
        // o que está acontecendo nessa fase de aprendizado, porém o ideal seria importar.
        Assertions.assertDoesNotThrow(() -> productService.delete(existingId));

        Mockito.verify(productRepositoryMock, Mockito.times(1)).deleteById(existingId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        // Confirma que está tratando a exception do repository e que está lançando a exception correta do service
        Assertions.assertThrows(ResourceNotFoundException.class, () -> productService.delete(nonExistingId));

        Mockito.verify(productRepositoryMock, Mockito.times(1)).deleteById(nonExistingId);
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(DatabaseException.class, () -> productService.delete(dependentId));

        Mockito.verify(productRepositoryMock, Mockito.times(1)).deleteById(dependentId);
    }

}
