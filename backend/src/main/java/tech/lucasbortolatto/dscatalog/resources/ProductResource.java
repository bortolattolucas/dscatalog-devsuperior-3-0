package tech.lucasbortolatto.dscatalog.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.lucasbortolatto.dscatalog.dto.ProductDTO;
import tech.lucasbortolatto.dscatalog.services.ProductService;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping(value = "/products")
public class ProductResource {

    @Autowired
    ProductService productService;

    //RequestParam: opcional na request
    @GetMapping
    // como no banco relacional o id começa por 1, se informar 0 traz todos sem problema, por isso esse é o id default
    public ResponseEntity<Page<ProductDTO>> findAll(@RequestParam(value = "categoryId", defaultValue = "0") Long categoryId,
                                                    @RequestParam(value = "name", defaultValue = "") String name,
                                                    Pageable pageable) {
        // a função trim() de um string remove os espaços em branco antes e depois da frase ou palavra
        return ResponseEntity.ok(productService.findAllPaged(categoryId, name.trim(), pageable));
    }

    //PathVariable: obrigatório na request
    @GetMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    // com essa anotacao @Valid, o service nem é chamado, o erro ja dispara na hra que recebe o argumento DTO
    // esses sao os casos onde apenas o valor do argumento é levado em consideração para a validação
    @PostMapping
    public ResponseEntity<ProductDTO> insert(@Valid @RequestBody ProductDTO dto) {
        dto = productService.insert(dto);
        //Para incluir o location do novo recurso nos headers da resposta
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(dto.getId()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        dto = productService.update(id, dto);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
