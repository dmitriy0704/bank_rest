package dev.folomkin.bankrest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerIT {


    @Autowired
    private WebApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }


    @Test
    public void whenPostRequestToUsersAndValidUser_thenCorrectResponseSignUp() throws Exception {
        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
        String user = "{\"username\": \"username\", \"email\" : \"testuser@mail.ru\", \"password\" : \"password\"}";
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .content(user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(applicationJson));
    }


    @Test
    public void whenPostRequestToUsersAndInValidUser_thenCorrectResponseSignUp() throws Exception {
        String user = "{\"username\": \"\", \"email\" : \"testuser@mail.ru\", \"password\" : \"password\"}";
        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .content(user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.username", Is.is("Имя пользователя должно содержать от 5 до 50 символов")))
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON));
    }


    @Test
    public void whenPostRequestToUsersAndValidUser_thenCorrectResponseSignIn() throws Exception {
        MediaType applicationJson = new MediaType(MediaType.APPLICATION_JSON);
        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .with(user("admnfalse")
                                .password("password12"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(applicationJson));
    }


    @Test
    public void whenPostRequestToUsersAndInValidUser_thenCorrectResponseSignIn() throws Exception {
        String user = "{\"username\": \"\", \"password\" : \"password\"}";
        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .content(user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.username", Is.is("Имя пользователя должно содержать от 5 до 50 символов")))
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON));
    }

}