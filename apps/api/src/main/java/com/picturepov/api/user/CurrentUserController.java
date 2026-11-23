package com.picturepov.api.user;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
class CurrentUserController {
    private final CurrentUserService currentUserService;

    CurrentUserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    @ApiResponse(responseCode = "200", description = "Signed-in user; created on the first call")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    CurrentUserResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return currentUserService.getCurrentUser(jwt);
    }
}
