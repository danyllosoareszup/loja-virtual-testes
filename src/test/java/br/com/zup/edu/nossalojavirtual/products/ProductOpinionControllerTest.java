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
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class ProductOpinionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductOpinionRepository productOpinionRepository;

    @Autowired
    private UserRepository userRepository;

    private Category category;

    private Product product;

    private User user;


    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(this.user = new User("danyllosiqueira@gmail.com"));
        categoryRepository.save(this.category = new Category("Computador"));

        PreProduct preProduct = new PreProduct(this.user, this.category, "Mouse", new BigDecimal("55.0"), 5, "gamer" );

        this.product = new Product(preProduct,
                List.of(new Photo("www.foto1.com"), new Photo("wwww.foto2.com")),
                Set.of(new Characteristic("cor", "preto"),
                        new Characteristic("luz", "rgb"),
                        new Characteristic("tipo", "gamer")));

        productRepository.save(this.product);
    }

    @Test
    @Transactional
    @DisplayName("must register a product opinion")
    void test1() throws Exception {

        NewOpinionRequest newOpinion = new NewOpinionRequest(3, "Qualidade", "muito hergonômico", this.product.getId());

        String payload = mapper.writeValueAsString(newOpinion);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt().jwt(builder -> {
                            builder.claim("email", this.user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_opinion:write")))
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        MockMvcResultMatchers.redirectedUrlPattern("/api/opinions/*")
                );

        List<ProductOpinion> opinions = productOpinionRepository.findAll();

        assertEquals(1, opinions.size());
    }

    @Test
    @Transactional
    @DisplayName("if the user does not exist, the product opinion must not be created")
    void test2() throws Exception {

        NewOpinionRequest newOpinion = new NewOpinionRequest(3, "Qualidade", "muito hergonômico", this.product.getId());

        String payload = mapper.writeValueAsString(newOpinion);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt().jwt(builder -> {
                            builder.claim("email", "mestreyuri@gmail.com");
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_opinion:write")))
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
    @DisplayName("if the product does not exist, the opinion should not be registered")
    void test3() throws Exception {

        NewOpinionRequest newOpinion = new NewOpinionRequest(3, "Qualidade", "muito hergonômico", UUID.randomUUID());

        String payload = mapper.writeValueAsString(newOpinion);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt().jwt(builder -> {
                            builder.claim("email", this.user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_opinion:write")))
                .header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        CustomErrorMessage errorMessage = mapper.readValue(payloadResponse, CustomErrorMessage.class);

        List<ProductOpinion> opinions = productOpinionRepository.findAll();

        assertEquals(0, opinions.size());
        assertEquals(1, errorMessage.getMensagens().size());
        assertThat(errorMessage.getMensagens(), containsInAnyOrder("productId: Category productId is not registered"));
    }

    @Test
    @Transactional
    @DisplayName("if the data is invalid, the products must not be registered")
    void test4() throws Exception {

        NewOpinionRequest newOpinion = new NewOpinionRequest(7, "", "a".repeat(501), null);

        String payload = mapper.writeValueAsString(newOpinion);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt().jwt(builder -> {
                            builder.claim("email", this.user.getUsername());
                        })
                        .authorities(new SimpleGrantedAuthority("SCOPE_opinion:write")))
                .header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        CustomErrorMessage errorMessage = mapper.readValue(payloadResponse, CustomErrorMessage.class);

        List<ProductOpinion> opinions = productOpinionRepository.findAll();

        assertEquals(0, opinions.size());
        assertEquals(4, errorMessage.getMensagens().size());
        assertThat(errorMessage.getMensagens(), containsInAnyOrder("rating: deve estar entre 1 e 5",
                                                                    "description: o comprimento deve ser entre 0 e 500",
                                                                    "title: não deve estar em branco",
                                                                    "productId: não deve ser nulo"));
    }

    @Test
    @DisplayName("must not register a opinion when the token is not sent")
    void test5() throws Exception{

        NewOpinionRequest newOpinion = new NewOpinionRequest(3, "Qualidade", "muito hergonômico", this.product.getId());

        String payload = mapper.writeValueAsString(newOpinion);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("should not register a opinion when the token does not have the proper scope")
    void test6() throws Exception{

        NewOpinionRequest newOpinion = new NewOpinionRequest(3, "Qualidade", "muito hergonômico", this.product.getId());

        String payload = mapper.writeValueAsString(newOpinion);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/opinions")
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
