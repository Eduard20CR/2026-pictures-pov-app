package com.picturepov.api.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    @Autowired MockMvc mockMvc;

    @Test
    void meWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/me/anything"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminWithNonAdminTokenReturns403() throws Exception {
        mockMvc.perform(get("/admin/anything").with(jwt()))
                .andExpect(status().isForbidden());
    }
}
