package com.picturepov.api.guest;

import com.picturepov.api.configuration.WebLayerTestConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the quota rules are in GuestQuotaServiceTest. */
@WebMvcTest(GuestQuotaController.class)
@Import(WebLayerTestConfiguration.class)
class GuestQuotaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean GuestQuotaService guestQuotaService;

    @Test
    void quotaIsAnsweredForTheGuestOfTheCookie() throws Exception {
        // Arrange
        UUID guestId = UUID.randomUUID();
        when(guestQuotaService.getUploadQuota("abc", guestId)).thenReturn(UploadQuotaResponse.of(10, 3));

        // Act
        ResultActions result = mockMvc.perform(get("/events/{code}/quota", "abc").servletPath("/events/abc/quota")
                .cookie(new Cookie("guest_id", guestId.toString())));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.used").value(3))
                .andExpect(jsonPath("$.remaining").value(7));
    }
}
