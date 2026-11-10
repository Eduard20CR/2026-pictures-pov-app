package com.picturepov.api.event;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasLength;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminEventControllerTest {

    @Autowired
    MockMvc mockMvc;

    private static final String VALID_BODY = """
            { "name": "Boda Ana y Luis", "eventDate": "2099-12-20", "ownerEmail": "Ana@Example.com" }
            """;

    @Test
    void adminCreatesDraftEventAssignedToOwner() throws Exception {
        // guion
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_admins"));

        // correr + revisar
        mockMvc.perform(post("/admin/events").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.publicCode", hasLength(22)))
                .andExpect(jsonPath("$.ownerEmail").value("ana@example.com"));
    }

    @Test
    void invalidFieldsReturn400WithFieldList() throws Exception {
        // guion
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_admins"));
        String badBody = """
                { "name": "", "ownerEmail": "not-an-email" }
                """;

        // correr + revisar
        mockMvc.perform(post("/admin/events").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.length()").value(3));
    }
}