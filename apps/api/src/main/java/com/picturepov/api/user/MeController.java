package com.picturepov.api.user;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
class MeController {
    private final UserService userService;

    MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @ApiResponse(responseCode = "200", description = "Signed-in user; created on the first call")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.findOrCreate(jwt);
        return new MeResponse(user.getId(), user.getEmail());
    }
}
