package de.shareit.shareitcore.infrastructure.adapter

import de.shareit.shareitcore.application.service.UserService
import de.shareit.shareitcore.ui.dto.UserDto
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3000"])
@RestController
@RequestMapping("/api/profile")
class ProfileRestController(
    private val userService: UserService
) {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getProfile(authToken: OAuth2AuthenticationToken): UserDto {
        val reg = authToken.authorizedClientRegistrationId
        val id  = authToken.principal.name
        return UserDto(
            email       = userService.findEmailByProvider(reg, id),
            displayName = userService.findDisplayNameByProvider(reg, id)
        )
    }

    @PutMapping("/email")
    @PreAuthorize("isAuthenticated()")
    fun updateEmail(
        authToken: OAuth2AuthenticationToken,
        @RequestBody @Valid dto: UserDto
    ): UserDto {
        val reg = authToken.authorizedClientRegistrationId
        val id  = authToken.principal.name
        userService.updateEmailForUser(reg, id, dto.email ?: "")
        return getProfile(authToken)
    }

    @PutMapping("/displayname")
    @PreAuthorize("isAuthenticated()")
    fun updateName(
        authToken: OAuth2AuthenticationToken,
        @RequestBody @Valid dto: UserDto
    ): UserDto {
        val reg = authToken.authorizedClientRegistrationId
        val id  = authToken.principal.name
        userService.updateDisplayNameForUser(reg, id, dto.displayName ?: "")
        return getProfile(authToken)
    }
}
