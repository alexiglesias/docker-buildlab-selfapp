package com.example.selfapplite;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests the web layer only: no MySQL and no RabbitMQ are needed.
 * The database and the broker are replaced by mocks (fake objects),
 * so these tests run in seconds on any machine, including GitHub Actions.
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void anonymousVisitorIsSentToLogin() throws Exception {
        mvc.perform(get("/"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotAddUsers() throws Exception {
        mvc.perform(post("/add").with(csrf())
                .param("username", "bob").param("email", "bob@example.com"))
           .andExpect(status().isForbidden());

        verify(userRepository, never()).save(any());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void adminAddsUserAndEventIsPublished() throws Exception {
        mvc.perform(post("/add").with(csrf())
                .param("username", "bob").param("email", "bob@example.com"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/"));

        verify(userRepository).save(any(User.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.USER_CREATED_QUEUE), anyString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void userIsStillSavedWhenRabbitMqIsDown() throws Exception {
        doThrow(new AmqpConnectException(new java.net.ConnectException("broker down")))
            .when(rabbitTemplate).convertAndSend(anyString(), anyString());

        mvc.perform(post("/add").with(csrf())
                .param("username", "bob").param("email", "bob@example.com"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/"));

        verify(userRepository).save(any(User.class));
    }
}
