package tech.lucasbortolatto.dscatalog.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.lucasbortolatto.dscatalog.dto.ProductDTO;
import tech.lucasbortolatto.dscatalog.services.ProductService;
import tech.lucasbortolatto.dscatalog.services.exceptions.DatabaseException;
import tech.lucasbortolatto.dscatalog.services.exceptions.ResourceNotFoundException;
import tech.lucasbortolatto.dscatalog.tests.Factory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Carrega apenas o contexto da camada web, sem componentes, services, etc, propria para teste de unidade de controlador
// By default, tests annotated with @WebMvcTest will also auto-configure Spring Security and MockMvc
// https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/autoconfigure/web/servlet/WebMvcTest.html
@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

    // Objeto pra fazer requests
    @Autowired
    private MockMvc mockMvc;

    // Como essa classe está subindo um determinado contexto (nesse caso o da camada web)
    // É utilizado o MockBean ao invés do Mock, para que ele se comporte como um singleton
    // No pequeno contexto específico desse teste
    @MockBean
    private ProductService productService;

    // Nesses objetos auxiliares como o mockmvc ou o objectmapper eh tranquilo usar autowired
    // Ja que nao vao ser beans da nossa classe testada em si
    @Autowired
    private ObjectMapper objectMapper;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private ProductDTO productDTO;
    private PageImpl<ProductDTO> page;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;

        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(productDTO));

        // ajuste temporário após capítulo do banco de dados, esse categoryId e name ai nao ta conferido
        when(productService.findAllPaged(1L, "name", any())).thenReturn(page);

        when(productService.findById(existingId)).thenReturn(productDTO);
        when(productService.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

        // a função eq(long) evita erro de InvalidUseOfMatchers
        // pq quando usa o any, os outros objetos não podem ser simples
        // dai essa função deixa o atributo compatível pra ser usado ao lado do any()
        when(productService.update(eq(existingId), any())).thenReturn(productDTO);
        when(productService.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

        doNothing().when(productService).delete(existingId);
        doThrow(ResourceNotFoundException.class).when(productService).delete(nonExistingId);
        doThrow(DatabaseException.class).when(productService).delete(dependentId);

        when(productService.insert(any())).thenReturn(productDTO);
    }

    @Test
    public void findAllShouldReturnPage() throws Exception {
        mockMvc.perform(get("/products")).andExpect(status().isOk());
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists() throws Exception {
        mockMvc.perform(get("/products/{id}", existingId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/products/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void insertShouldReturnProductDTOCreated() throws Exception {
        mockMvc.perform(post("/products")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO))) // tbm da pra simplificar assim ao inves de criar um obj
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        mockMvc.perform(put("/products/{id}", existingId)
                        .accept(MediaType.APPLICATION_JSON)      // resposta que aceita
                        .contentType(MediaType.APPLICATION_JSON) // corpo que manda
                        .content(jsonBody))                      // objeto no corpo
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        mockMvc.perform(put("/products/{id}", nonExistingId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
        mockMvc.perform(delete("/products/{id}", existingId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        mockMvc.perform(delete("/products/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }
}
