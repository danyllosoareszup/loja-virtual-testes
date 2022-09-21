package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import br.com.zup.edu.nossalojavirtual.util.CustomErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Category category;

    private NewProductRequest productRequest;

    private User user;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(this.user = new User("danyllosiqueira@gmail.com"));

        categoryRepository.save(this.category = new Category("Computador"));

        this.productRequest = new NewProductRequest("mouse",
                new BigDecimal("55.0"),
                5,
                List.of("www.foto1.com", "www.foto2.com"),
                List.of(new NewCharacteristicRequest("iluminação", "rgb"),
                        new NewCharacteristicRequest("dpi", "8000"),
                        new NewCharacteristicRequest("cor", "preto")),
                "Gamer",
                this.category.getId());
    }

    @Test
    @Transactional
    @DisplayName("must register a product")
    void test1() throws Exception {

        String payload = mapper.writeValueAsString(this.productRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt().jwt(builder -> {
                        builder.claim("email", this.user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_product:write")))
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        MockMvcResultMatchers.redirectedUrlPattern("/api/products/*")
                );

        List<Product> products = productRepository.findAll();
        Product product = products.get(0);
        List<Photo> photos = product.getPhotos();
        Set<Characteristic> characteristics = product.getCharacteristics();

        assertEquals(1, products.size());
        assertEquals(2, photos.size());
        assertEquals(3, characteristics.size());
    }

    @Test
    @Transactional
    @DisplayName("if the user does not exist, the product must not be created")
    void test2() throws Exception {

        String payload = mapper.writeValueAsString(this.productRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt().jwt(builder -> {
                            builder.claim("email", "joaohenrique@gmail.com");
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_product:write")))
                .header("Accept-Language", "pt-br");

        Exception  resolvedException = mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isUnprocessableEntity()
                )
                .andReturn().getResolvedException();

        assertNotNull(resolvedException);
        assertEquals(ResponseStatusException.class,resolvedException.getClass());
        assertEquals("usuário não registrado",((ResponseStatusException) resolvedException).getReason());
    }

    @Test
    @Transactional
    @DisplayName("if the category does not exist, the product must not be registered")
    void test3() throws Exception {

        NewProductRequest invalidCategoryProduct = new NewProductRequest("mouse",
                new BigDecimal("55.0"),
                5,
                List.of("www.foto1.com", "www.foto2.com"),
                List.of(new NewCharacteristicRequest("iluminação", "rgb"),
                        new NewCharacteristicRequest("dpi", "8000"),
                        new NewCharacteristicRequest("cor", "preto")),
                "Gamer",
                Long.MAX_VALUE);

        String payload = mapper.writeValueAsString(invalidCategoryProduct);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt().jwt(builder -> {
                            builder.claim("email", this.user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_product:write")))
                .header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        CustomErrorMessage errorMessage = mapper.readValue(payloadResponse, CustomErrorMessage.class);

        List<Product> products = productRepository.findAll();

        assertEquals(0, products.size());
        assertEquals(1, errorMessage.getMensagens().size());
        assertThat(errorMessage.getMensagens(), containsInAnyOrder("categoryId: Category categoryId is not registered"));
    }

    @Test
    @Transactional
    @DisplayName("Do not register products with invalid data")
    void test4() throws Exception {

        NewProductRequest invalidProduct = new NewProductRequest("",
                new BigDecimal("0.0001"),
                -9,
                List.of(),
                List.of(new NewCharacteristicRequest("iluminação", "rgb"),
                        new NewCharacteristicRequest("dpi", "8000")),
                "a".repeat(1001),
                this.category.getId());

        String payload = mapper.writeValueAsString(invalidProduct);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt().jwt(builder -> {
                            builder.claim("email", this.user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_product:write")))
                .header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        CustomErrorMessage errorMessage = mapper.readValue(payloadResponse, CustomErrorMessage.class);

        List<Product> products = productRepository.findAll();

        assertEquals(0, products.size());
        assertEquals(6, errorMessage.getMensagens().size());
        assertThat(errorMessage.getMensagens(), containsInAnyOrder("stockQuantity: deve ser maior que ou igual à 0",
                                                                            "characteristics: tamanho deve ser entre 3 e 2147483647",
                                                                            "description: o comprimento deve ser entre 0 e 1000",
                                                                            "price: deve ser maior que ou igual a 0.01",
                                                                            "name: não deve estar em branco",
                                                                            "photos: tamanho deve ser entre 1 e 2147483647"));
    }

    @Test
    @DisplayName("must not register a product when the token is not sent")
    void test5() throws Exception{

        String payload = mapper.writeValueAsString(this.productRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("should not register a product when the token does not have the proper scope")
    void test6() throws Exception{

        String payload = mapper.writeValueAsString(this.productRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt().jwt(builder -> {
                            builder.claim("email", this.user.getUsername());
                        }))
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isForbidden());
    }






}
