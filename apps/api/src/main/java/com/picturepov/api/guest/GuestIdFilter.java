package com.picturepov.api.guest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Component
public class GuestIdFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "guest_id";

    public static final String ATTRIBUTE = "guestId";
    private static final Duration MAX_AGE = Duration.ofDays(30);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        UUID guestId = readGuestId(request);

        if (guestId == null) {
            guestId = UUID.randomUUID();
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, guestId.toString())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/api")
                    .maxAge(MAX_AGE)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        request.setAttribute(ATTRIBUTE, guestId);
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getServletPath().startsWith("/events/");
    }

    private static UUID readGuestId(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie == null) {
            return null;
        }
        try {
            return UUID.fromString(cookie.getValue());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

