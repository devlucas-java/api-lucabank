package com.github.devlucasjava.apilucabank.controller;

import com.github.devlucasjava.apilucabank.dto.response.UsersResponse;
import com.github.devlucasjava.apilucabank.model.Users;
import com.github.devlucasjava.apilucabank.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "Endpoints related to users")
public class UsersController {

    private final UsersService usersService;

    @Operation(summary = "Get authenticated user", description = "Returns the currently authenticated user's data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authenticated user returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/profile")
    public ResponseEntity<UsersResponse> getMe(@AuthenticationPrincipal Users users) {

        log.debug("Fetching authenticated user profile: {}", users.getEmail());

        UsersResponse userResponse = usersService.getAuthenticatedUser(users);

        log.debug("Authenticated user profile returned successfully: {}", users.getEmail());

        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }
}