package tech.lucasbortolatto.dscatalog.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tech.lucasbortolatto.dscatalog.dto.ProductDTO;
import tech.lucasbortolatto.dscatalog.services.ProductService;
import tech.lucasbortolatto.dscatalog.services.exceptions.DatabaseException;
import tech.lucasbortolatto.dscatalog.services.exceptions.ResourceNotFoundException;
import tech.lucasbortolatto.dscatalog.tests.Factory;
import tech.lucasbortolatto.dscatalog.tests.TokenUtil;

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
// @WebMvcTest(ProductResource.class) -> anotação comentada pela explicação abaixo
// removido porque agora com security precisa carregar o contexto, mas deixado aqui por documentação de conhecimento

// anotações adicionadas após necessidade por conta do security (precisar das instancias dos componentes do contexto)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductResourceTests {

    @Autowired
    private TokenUtil tokenUtil;

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
    private String username;
    private String password;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        username = "maria@gmail.com";
        password = "123456";

        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(productDTO));

        when(productService.findAllPaged(eq(0L), eq(""), any())).thenReturn(page);

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
        String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
        mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + accessToken)
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
        String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        mockMvc.perform(put("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + accessToken)
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
        String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        mockMvc.perform(put("/products/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
        String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

        mockMvc.perform(delete("/products/{id}", existingId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

        mockMvc.perform(delete("/products/{id}", nonExistingId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
