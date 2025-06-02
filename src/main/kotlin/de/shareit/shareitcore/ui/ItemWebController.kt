package de.shareit.shareitcore.ui

import de.shareit.shareitcore.application.ListingService
import de.shareit.shareitcore.domain.model.AppUser
import de.shareit.shareitcore.domain.model.PriceUnit
import de.shareit.shareitcore.domain.service.UserRepository
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import de.shareit.shareitcore.web.dto.ItemDto
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException


@Controller
@RequestMapping("/items")
class ItemWebController(
    private val listingService: ListingService,
    private val userRepo: UserRepository
) {

    /**
     * Liste aller Items anzeigen.
     */
    @GetMapping
    fun listAll(model: Model): String {
        val allItems: List<ItemResponseDto> = listingService.findAll()
        model.addAttribute("items", allItems)
        return "items"
    }

    /**
     * Formular für neues Item anzeigen.
     */
    @GetMapping("/new")
    fun showCreateForm(model: Model): String {
        model.addAttribute(
            "itemDto",
            ItemDto(
                title = "",
                description = null,
                priceAmount = 0.toBigDecimal(),
                priceUnit =  PriceUnit.DAILY,
                address = "")
        )
        model.addAttribute("editMode", false)
        return "item-form"
    }

    /**
     * Neues Item anlegen.
     */
    @PostMapping("/new")
    fun createItem(
        authToken: OAuth2AuthenticationToken?,
        @Valid @ModelAttribute itemDto: ItemDto,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (authToken == null) {
            model.addAttribute("errorMessage", "Du musst eingeloggt sein, um ein Item anzulegen.")
            return "item-form"
        }

        if (bindingResult.hasErrors()) {
            return "item-form"
        }

        // Aktuellen User (AppUser) ermitteln
        val registrationId = authToken.authorizedClientRegistrationId
        val providerId     = authToken.principal.name
        val owner: AppUser = userRepo
            .findByOauthProviderAndProviderId(registrationId, providerId)
            ?: throw IllegalArgumentException("Angemeldeter Nutzer nicht gefunden")

        listingService.createItem(owner.id!!, itemDto)
        return "redirect:/items"
    }

    /**
     * Formular zum Bearbeiten eines bestehenden Items anzeigen.
     */
    @GetMapping("/{id}/edit")
    fun showEditForm(
        @PathVariable id: Long,
        authToken: OAuth2AuthenticationToken?,
        model: Model
    ): String {
        if (authToken == null) {
            return "redirect:/items"
        }
        // 1. Aktuellen AppUser ermitteln
        val registrationId = authToken.authorizedClientRegistrationId
        val providerId = authToken.principal.name
        val ownerEntity = userRepo.findByOauthProviderAndProviderId(registrationId, providerId)
            ?: throw IllegalArgumentException("Aktueller User nicht gefunden")

        // 2. Bestehendes Item laden
        val existingDto: ItemResponseDto = listingService.findById(id)
        if (existingDto.ownerId != ownerEntity.id) {
            // Falls ein anderer User versucht zu editieren
            return "redirect:/items"
        }

        // 3. ItemResponseDto → ItemDto konvertieren
        val itemDto = ItemDto(
            title = existingDto.title,
            description = existingDto.description,
            priceAmount = existingDto.priceAmount,
            priceUnit = existingDto.priceUnit,
            address = existingDto.address
        )

        model.addAttribute("itemDto", itemDto)
        model.addAttribute("itemId", id)
        model.addAttribute("editMode", true)
        return "item-form"
    }

    /**
     * Aktualisiere ein bestehendes Item
     */
    @PostMapping("/{id}/edit")
    fun updateItem(
        @PathVariable id: Long,
        authToken: OAuth2AuthenticationToken?,
        @Valid @ModelAttribute itemDto: ItemDto,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (authToken == null) {
            model.addAttribute("errorMessage", "Du musst eingeloggt sein, um ein Item zu bearbeiten.")
            return "item-form"
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("itemId", id)
            return "item-form"
        }

        // Aktuellen User ermitteln
        val registrationId = authToken.authorizedClientRegistrationId
        val providerId = authToken.principal.name
        val owner: AppUser = userRepo
            .findByOauthProviderAndProviderId(registrationId, providerId)
            ?: throw IllegalArgumentException("Angemeldeter Nutzer nicht gefunden")

        listingService.updateItem(owner.id!!, id, itemDto)
        return "redirect:/items/$id"
    }

    /**
     * Item löschen (nur Owner)
     */
    @PostMapping("/{id}/delete")
    fun deleteItem(
        @PathVariable id: Long,
        authToken: OAuth2AuthenticationToken?
    ): String {
        if (authToken == null) {
            return "redirect:/items"
        }

        // Aktuellen User ermitteln
        val registrationId = authToken.authorizedClientRegistrationId
        val providerId = authToken.principal.name
        val owner: AppUser = userRepo
            .findByOauthProviderAndProviderId(registrationId, providerId)
            ?: throw IllegalArgumentException("Angemeldeter Nutzer nicht gefunden")

        listingService.deleteItem(owner.id!!, id)
        return "redirect:/items"
    }
}
