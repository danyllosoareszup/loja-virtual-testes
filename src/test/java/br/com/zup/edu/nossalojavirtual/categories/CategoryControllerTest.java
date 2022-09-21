package br.com.zup.edu.nossalojavirtual.categories;

import br.com.zup.edu.nossalojavirtual.users.User;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    private NewCategoryRequest newCategory;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        categoryRepository.save(this.category = new Category("Computador"));

        this.newCategory = new NewCategoryRequest("Perifericos", this.category.getId());
    }

    @Test
    @DisplayName("must register a category")
    void test1() throws Exception {

        String payload = mapper.writeValueAsString(this.newCategory);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        MockMvcResultMatchers.redirectedUrlPattern("/api/categories/*")
                );

        List<Category> categories = categoryRepository.findAll();

        assertEquals(2, categories.size());
        assertEquals(this.newCategory.getName(), categories.get(1).getName());
        assertEquals(this.category.getId(), categories.get(1).getSuperCategory().getId());
    }

    @Test
    @DisplayName("must not register a category that already exists in the system")
    void test2() throws Exception {

        NewCategoryRequest newCategory = new NewCategoryRequest("Computador", null);

        String payload = mapper.writeValueAsString(newCategory);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
                .header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        CustomErrorMessage errorMessage = mapper.readValue(payloadResponse, CustomErrorMessage.class);

        assertEquals(1, errorMessage.getMensagens().size());
        assertThat(errorMessage.getMensagens(), containsInAnyOrder("name: name is already registered"));
    }

    @Test
    @DisplayName("should not register category with invalid data")
    void test3() throws Exception {

        NewCategoryRequest newCategory = new NewCategoryRequest(null, Long.MAX_VALUE);

        String payload = mapper.writeValueAsString(newCategory);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
                .header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        CustomErrorMessage errorMessage = mapper.readValue(payloadResponse, CustomErrorMessage.class);

        assertEquals(2, errorMessage.getMensagens().size());
        assertThat(errorMessage.getMensagens(), containsInAnyOrder("superCategory: The super category does not exists",
                                                                            "name: não deve estar vazio"));
    }

    @Test
    @DisplayName("must not register a category when the token is not sent")
    void test4() throws Exception{

        String payload = mapper.writeValueAsString(this.newCategory);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("should not register a category when the token does not have the proper scope")
    void test5() throws Exception{

        String payload = mapper.writeValueAsString(this.newCategory);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt())
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isForbidden());
    }


}
