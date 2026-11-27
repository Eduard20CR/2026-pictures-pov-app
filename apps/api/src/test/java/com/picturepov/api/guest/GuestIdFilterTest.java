package com.picturepov.api.guest;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GuestIdFilterTest {

    GuestIdFilter sut = new GuestIdFilter();

    @Test
    void newGuestGetsASecureCookie() throws Exception {
        // Arrange
        MockHttpServletRequest request = guestRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        sut.doFilter(request, response, new MockFilterChain());

        // Assert
        UUID guestId = (UUID) request.getAttribute(GuestIdFilter.ATTRIBUTE);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .startsWith("guest_id=" + guestId)
                .contains("HttpOnly", "Secure", "SameSite=Lax", "Path=/api");
    }

    @Test
    void returningGuestKeepsTheSameId() throws Exception {
        // Arrange
        UUID existing = UUID.randomUUID();
        MockHttpServletRequest request = guestRequest();
        request.setCookies(new Cookie("guest_id", existing.toString()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        sut.doFilter(request, response, new MockFilterChain());

        // Assert
        assertThat(request.getAttribute(GuestIdFilter.ATTRIBUTE)).isEqualTo(existing);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void invalidCookieGetsANewId() throws Exception {
        // Arrange
        MockHttpServletRequest request = guestRequest();
        request.setCookies(new Cookie("guest_id", "not-a-uuid"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        sut.doFilter(request, response, new MockFilterChain());

        // Assert
        assertThat(request.getAttribute(GuestIdFilter.ATTRIBUTE)).isInstanceOf(UUID.class);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).startsWith("guest_id=");
    }

    @Test
    void ignoresRoutesOutsideEvents() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/events");
        request.setServletPath("/admin/events");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        sut.doFilter(request, response, new MockFilterChain());

        // Assert
        assertThat(request.getAttribute(GuestIdFilter.ATTRIBUTE)).isNull();
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    private static MockHttpServletRequest guestRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/events/abc/quota");
        request.setServletPath("/events/abc/quota");
        return request;
    }
}
