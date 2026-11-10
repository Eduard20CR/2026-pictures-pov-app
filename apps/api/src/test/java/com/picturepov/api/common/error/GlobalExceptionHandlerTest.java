package com.picturepov.api.common.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.picturepov.api.guest.GuestIdFilter;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;

    @Test
    void malformedJsonReturns400WithCode() throws Exception {
        // run
        var request = post("/events/any-code/uploads")
                .requestAttr(GuestIdFilter.ATTRIBUTE, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"contentType\": ");

        // run
        mockMvc.perform(request)
                // check
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST_BODY"));
    }

}