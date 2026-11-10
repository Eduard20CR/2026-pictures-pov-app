package com.picturepov.api.event;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasLength;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminEventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EventRepository eventRepository;

    private static final String VALID_BODY = """
            { "name": "Boda Ana y Luis", "eventDate": "2099-12-20", "ownerEmail": "Ana@Example.com" }
            """;

    @Test
    void adminCreatesDraftEventAssignedToOwner() throws Exception {
        // correr + revisar
        mockMvc.perform(post("/admin/events").with(asAdmin())
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
        String badBody = """
                { "name": "", "ownerEmail": "not-an-email" }
                """;

        // correr + revisar
        mockMvc.perform(post("/admin/events").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    void adminListsEventsNewestFirst() throws Exception {
        // guion
        createEvent("Old party", "2099-01-10");
        createEvent("New party", "2099-12-31");

        // correr + revisar
        mockMvc.perform(get("/admin/events").with(asAdmin()).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("New party"))
                .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(2)));
    }

    @Test
    void sizeOver100Returns400() throws Exception {
        // correr + revisar
        mockMvc.perform(get("/admin/events").with(asAdmin()).param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("size"));
    }

    @Test
    void eventWithoutOwnerIsListedWithNullEmail() throws Exception {
        // guion
        eventRepository.save(Event.draft("No owner party", LocalDate.of(2099, 12, 31), "noOwnerCode00000000000"));

        // correr + revisar
        mockMvc.perform(get("/admin/events").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("No owner party"))
                .andExpect(jsonPath("$.items[0].ownerEmail").doesNotExist());
    }

    private static RequestPostProcessor asAdmin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_admins"));
    }

    private void createEvent(String name, String date) throws Exception {
        mockMvc.perform(post("/admin/events").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "%s", "eventDate": "%s", "ownerEmail": "owner@example.com" }
                                """.formatted(name, date)))
                .andExpect(status().isCreated());
    }
}
