package br.com.zup.edu.nossalojavirtual.users;

import br.com.zup.edu.nossalojavirtual.util.CustomErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class UserControllerTest {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private NewUserRequest newUserRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        this.newUserRequest = new NewUserRequest("danyllosiqueira@gmail.com");
    }

    @Test
    @DisplayName("should not register user with invalid data")
    void test1() throws Exception{


        NewUserRequest newUserInvalido = new NewUserRequest("");

        String payload = mapper.writeValueAsString(newUserInvalido);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_users:write")))
                .header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        CustomErrorMessage errorMessage = mapper.readValue(payloadResponse, CustomErrorMessage.class);

        List<User> users = userRepository.findAll();

        assertEquals(0, users.size());
        assertEquals(1, errorMessage.getMensagens().size());
        assertThat(errorMessage.getMensagens(), containsInAnyOrder("login: não deve estar vazio"));
    }

    @Test
    @DisplayName("You should not register a user with invalid email")
    void test2() throws Exception{


        NewUserRequest newUserInvalido = new NewUserRequest("danyllo.com.br");

        String payload = mapper.writeValueAsString(newUserInvalido);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_users:write")))
                .header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        CustomErrorMessage errorMessage = mapper.readValue(payloadResponse, CustomErrorMessage.class);

        List<User> users = userRepository.findAll();

        assertEquals(0, users.size());
        assertEquals(1, errorMessage.getMensagens().size());
        assertThat(errorMessage.getMensagens(), containsInAnyOrder("login: deve ser um endereço de e-mail bem formado"));
    }

    @Test
    @DisplayName("must not register a user when the token is not sent")
    void test3() throws Exception{

        String payload = mapper.writeValueAsString(this.newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("should not register a user when the token does not have the proper scope")
    void test4() throws Exception{

        String payload = mapper.writeValueAsString(this.newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt())
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isForbidden());
    }


    @Test
    @DisplayName("must register a user with valid data")
    void test5() throws Exception{

        String payload = mapper.writeValueAsString(this.newUserRequest);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_users:write")))
                .header("Accept-Language", "pt-br");

        mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isCreated()
                )
                .andExpect(
                        MockMvcResultMatchers.redirectedUrlPattern("/api/users/*")
                );

        List<User> users = userRepository.findAll();

        assertEquals(1, users.size());
        assertEquals("danyllosiqueira@gmail.com", users.get(0).getUsername());
    }

    @Test
    @DirtiesContext
    @DisplayName("should not register a user when there is already a user with the same email in the system")
    void test6() throws Exception{

        User user = new User("danyllosiqueira@gmail.com");

        userRepository.save(user);

        NewUserRequest newUserEmailRepetido = new NewUserRequest("danyllosiqueira@gmail.com");

        String payload = mapper.writeValueAsString(newUserEmailRepetido);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .with(jwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_users:write")))
                .header("Accept-Language", "pt-br");

        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest()
                )
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        CustomErrorMessage errorMessage = mapper.readValue(payloadResponse, CustomErrorMessage.class);

        assertEquals(1, errorMessage.getMensagens().size());
        assertThat(errorMessage.getMensagens(), containsInAnyOrder("login: login is already registered"));
    }
}
