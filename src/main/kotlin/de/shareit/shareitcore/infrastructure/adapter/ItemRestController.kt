package de.shareit.shareitcore.infrastructure.adapter

import de.shareit.shareitcore.application.service.ListingService
import de.shareit.shareitcore.domain.service.UserRepository
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import de.shareit.shareitcore.web.dto.ItemDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
@RequestMapping("/api/items")
class ItemRestController(
    private val listingService: ListingService,
    private val userRepo: UserRepository
) {

    @GetMapping
    fun getAll(): List<ItemResponseDto> = listingService.findAll()

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): ItemResponseDto =
        listingService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    fun create(
        authToken: OAuth2AuthenticationToken,
        @RequestBody @Valid dto: ItemDto
    ): ItemResponseDto {
        val user = findCurrentUser(authToken)
        return listingService.createItem(user.id!!, dto)
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun update(
        @PathVariable id: Long,
        authToken: OAuth2AuthenticationToken,
        @RequestBody @Valid dto: ItemDto
    ): ItemResponseDto {
        val user = findCurrentUser(authToken)
        listingService.updateItem(user.id!!, id, dto)
        return listingService.findById(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    fun delete(
        @PathVariable id: Long,
        authToken: OAuth2AuthenticationToken
    ) {
        val user = findCurrentUser(authToken)
        listingService.deleteItem(user.id!!, id)
    }

    private fun findCurrentUser(token: OAuth2AuthenticationToken) =
        userRepo
            .findByOauthProviderAndProviderId(
                token.authorizedClientRegistrationId,
                token.principal.name
            )
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nutzer nicht gefunden")
}