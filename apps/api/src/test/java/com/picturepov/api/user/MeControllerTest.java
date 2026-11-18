package com.picturepov.api.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @MockitoBean CognitoUserInfoClient userInfoClient;


    @Test
    void firstRequestCreatesUserWithCognitoSub() throws Exception {
        // guion
        UUID sub = UUID.randomUUID();
        when(userInfoClient.fetch(anyString())).thenReturn(new CognitoUserInfoClient.UserInfo("new@example.com", "true"));

        // correr
        mockMvc.perform(get("/me").with(jwt().jwt(token -> token.subject(sub.toString()))))
                .andExpect(status().isOk());

        // revisar
        assertThat(userRepository.findById(sub)).isPresent();
    }

    @Test
    void unexpectedFailureReturnsProblemWithoutInternalDetails() throws Exception {
        // guion
        when(userInfoClient.fetch(anyString())).thenThrow(new IllegalStateException("Cognito userInfo returned no email"));

        // correr + revisar
        mockMvc.perform(get("/me").with(jwt().jwt(token -> token.subject(UUID.randomUUID().toString()))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value("Unexpected error."));
    }
}
