package com.picturepov.api.event;

import java.time.LocalDate;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    JdbcClient jdbc;

    private static final String VALID_BODY = """
            { "name": "Boda Ana y Luis", "eventDate": "2099-12-20", "organizerEmail": "Ana@Example.com" }
            """;

    @Test
    void adminCreatesOpenEventAssignedToOrganizer() throws Exception {
        // correr + revisar
        mockMvc.perform(post("/admin/events").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.publicCode", hasLength(22)))
                .andExpect(jsonPath("$.organizerEmail").value("ana@example.com"));
    }

    @Test
    void guestsReachANewEventRightAway() throws Exception {
        // guion
        String publicCode = createEvent("Fiesta", "2099-06-01");

        // correr + revisar
        mockMvc.perform(get("/events/{code}", publicCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fiesta"));
    }

    @Test
    void invalidFieldsReturn400WithFieldList() throws Exception {
        // guion
        String badBody = """
                { "name": "", "organizerEmail": "not-an-email" }
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
    void eventWithoutOrganizerIsListedWithNullEmail() throws Exception {
        // guion
        eventRepository.save(Event.open("No organizer party", LocalDate.of(2099, 12, 31), "noOrganizerCode0000000"));

        // correr + revisar
        mockMvc.perform(get("/admin/events").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("No organizer party"))
                .andExpect(jsonPath("$.items[0].organizerEmail").doesNotExist());
    }

    @Test
    void deletedEventIsNotListedAndAnswers404() throws Exception {
        // guion
        String publicCode = createEvent("Deleted party", "2099-12-31");
        eventRepository.flush();
        int deleted = jdbc.sql("UPDATE events SET deleted_at = now() WHERE public_code = :code").param("code", publicCode).update();
        assertThat(deleted).isEqualTo(1);

        // correr + revisar
        mockMvc.perform(get("/admin/events").with(asAdmin()).param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.publicCode == '%s')]".formatted(publicCode)).isEmpty());
        mockMvc.perform(get("/events/{code}", publicCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
    }

    private static RequestPostProcessor asAdmin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_admins"));
    }

    /** Creates an event through the API and returns its public code. */
    private String createEvent(String name, String date) throws Exception {
        String body = mockMvc.perform(post("/admin/events").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "%s", "eventDate": "%s", "organizerEmail": "organizer@example.com" }
                                """.formatted(name, date)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.publicCode");
    }
}
