package de.shareit.shareitcore.ui

import de.shareit.shareitcore.application.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ProfileController(
    private val userService: UserService
) {


    @GetMapping("/profile")
    fun showProfile(
        model: Model,
        authToken: OAuth2AuthenticationToken
    ): String {
        val registrationId = authToken.authorizedClientRegistrationId
        val principal      = authToken.principal
        val providerId     = principal.name

        val currentEmail = userService.findEmailByProvider(registrationId, providerId)
        
        val dto = UserDto(email = currentEmail)
        model.addAttribute("userDto", dto)
        return "profile"
    }

    @PostMapping("/profile")
    fun updateEmail(
        @ModelAttribute userDto: UserDto,
        authToken: OAuth2AuthenticationToken,
        model: Model
    ): String {
        val registrationId = authToken.authorizedClientRegistrationId
        val providerId     = authToken.principal.name

        return try {
            userService.updateEmailForUser(registrationId, providerId, userDto.email ?: "")
            model.addAttribute("successMessage", "E-Mail erfolgreich aktualisiert.")
        } catch (ex: Exception) {
            model.addAttribute("errorMessage", "Fehler beim Speichern der E-Mail: ${ex.message}")
        }.let {
            showProfile(model, authToken)
        }
    }
}