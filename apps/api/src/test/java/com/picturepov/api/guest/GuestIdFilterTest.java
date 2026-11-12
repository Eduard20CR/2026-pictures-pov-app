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

    GuestIdFilter filter = new GuestIdFilter();

    @Test
    void newGuestGetsASecureCookie() throws Exception {
        MockHttpServletRequest request = guestRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String cookie = response.getHeader(HttpHeaders.SET_COOKIE);
        UUID guestId = (UUID) request.getAttribute(GuestIdFilter.ATTRIBUTE);
        assertThat(cookie)
                .startsWith("guest_id=" + guestId)
                .contains("HttpOnly", "Secure", "SameSite=Lax", "Path=/api");
    }

    @Test
    void returningGuestKeepsTheSameId() throws Exception {
        UUID existing = UUID.randomUUID();
        MockHttpServletRequest request = guestRequest();
        request.setCookies(new Cookie("guest_id", existing.toString()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getAttribute(GuestIdFilter.ATTRIBUTE)).isEqualTo(existing);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void invalidCookieGetsANewId() throws Exception {
        MockHttpServletRequest request = guestRequest();
        request.setCookies(new Cookie("guest_id", "not-a-uuid"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getAttribute(GuestIdFilter.ATTRIBUTE)).isInstanceOf(UUID.class);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).startsWith("guest_id=");
    }

    @Test
    void ignoresRoutesOutsideEvents() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/events");
        request.setServletPath("/admin/events");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getAttribute(GuestIdFilter.ATTRIBUTE)).isNull();
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    private static MockHttpServletRequest guestRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/events/abc/quota");
        request.setServletPath("/events/abc/quota");
        return request;
    }
}
