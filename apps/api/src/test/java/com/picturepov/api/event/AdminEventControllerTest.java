package com.picturepov.api.event;

import com.picturepov.api.configuration.WebLayerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the event rules are in AdminEventServiceTest. */
@WebMvcTest(AdminEventController.class)
@Import(WebLayerTestConfiguration.class)
class AdminEventControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AdminEventService adminEventService;

    AdminEventResponse event = new AdminEventResponse(UUID.randomUUID(), "bodaAnaYLuis0000000000", "Boda Ana y Luis",
            LocalDate.of(2099, 12, 20), EventStatus.OPEN, "ana@example.com");

    @Test
    void creatingAnEventAnswers201WithTheEvent() throws Exception {
        // Arrange
        when(adminEventService.createEvent(new CreateEventRequest("Boda Ana y Luis", LocalDate.of(2099, 12, 20), "ana@example.com")))
                .thenReturn(event);

        // Act
        ResultActions result = mockMvc.perform(post("/admin/events").with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "Boda Ana y Luis", "eventDate": "2099-12-20", "organizerEmail": "ana@example.com" }
                        """));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(event.id().toString()))
                .andExpect(jsonPath("$.publicCode").value("bodaAnaYLuis0000000000"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.organizerEmail").value("ana@example.com"));
    }

    @Test
    void invalidFieldsAnswer400WithEachFieldAndCreateNothing() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(post("/admin/events").with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "", "organizerEmail": "not-an-email" }
                        """));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.length()").value(3));
        verifyNoInteractions(adminEventService);
    }

    @Test
    void listingAnswers200WithTheRequestedPage() throws Exception {
        // Arrange
        when(adminEventService.listAllEvents(1, 5)).thenReturn(new AdminEventPage(List.of(event), 1, 5, 6, 2));

        // Act
        ResultActions result = mockMvc.perform(get("/admin/events").with(asAdmin()).param("page", "1").param("size", "5"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Boda Ana y Luis"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalItems").value(6))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void pageSizeOver100Answers400() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/admin/events").with(asAdmin()).param("size", "101"));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("size"));
        verifyNoInteractions(adminEventService);
    }

    @Test
    void signedInUserWhoIsNotAnAdminGets403() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(post("/admin/events").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // Assert
        result.andExpect(status().isForbidden());
        verifyNoInteractions(adminEventService);
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/admin/events"));

        // Assert
        result.andExpect(status().isUnauthorized());
        verifyNoInteractions(adminEventService);
    }

    private static RequestPostProcessor asAdmin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_admins"));
    }
}
