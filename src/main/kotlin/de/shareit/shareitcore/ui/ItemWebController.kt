package de.shareit.shareitcore.ui

import de.shareit.shareitcore.application.CategoryService
import de.shareit.shareitcore.application.ImageService
import de.shareit.shareitcore.application.ListingService
import de.shareit.shareitcore.domain.model.AppUser
import de.shareit.shareitcore.domain.model.PriceUnit
import de.shareit.shareitcore.domain.service.UserRepository
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import de.shareit.shareitcore.web.dto.ItemDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.lang.IllegalArgumentException
import org.springframework.http.HttpHeaders


@Controller
@RequestMapping("/items")
class ItemWebController(
    private val listingService: ListingService,
    private val userRepo: UserRepository,
    private val imageService: ImageService,
    private val categoryService: CategoryService,
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


    @GetMapping("/{id}")
    fun showDetail(@PathVariable id: Long, model: Model): String {
        val itemDto = listingService.findById(id)
        model.addAttribute("item", itemDto)

        val images = imageService.listImagesForItem(id)
        model.addAttribute("images", images)
        return "item-detail"    // Name des Thymeleaf‐Templates (item-detail.html)
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
        model.addAttribute("allCategories", categoryService.getAllCategories())
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
        @RequestParam("images", required = false) images: List<MultipartFile>?,  // NEU
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

        // 1. Item erstellen (gibt zurück, z. B. die neue Item-ID oder DTO)
        val createdItem: ItemResponseDto = listingService.createItem(owner.id!!, itemDto, itemDto.categoryId)

        // 2. Falls Bilder ausgewählt wurden, speichere sie
        images
            ?.filter { file -> !file.isEmpty }
            ?.let { fileList ->
                imageService.uploadImages(createdItem.id, fileList)
            }

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
            address = existingDto.address,
            latitude = existingDto.latitude,
            longitude = existingDto.longitude
        )

        model.addAttribute("itemDto", itemDto)
        model.addAttribute("itemId", id)
        model.addAttribute("editMode", true)

        // NEU: Bestehende Bilder (inkl. Thumbnail-Info) ins Model packen
        model.addAttribute("item", existingDto)

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
        @RequestParam("images", required = false) images: List<MultipartFile>?,   // NEU
        @RequestParam("thumbnailId", required = false) thumbnailId: Long?,           // NEU
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

        // 1. Item‐Daten aktualisieren
        listingService.updateItem(owner.id!!, id, itemDto)

        // 2. Falls neue Bilder ausgewählt wurden, speichere sie
        images
            ?.filter { file -> !file.isEmpty }
            ?.let { fileList ->
                imageService.uploadImages(id, fileList)
            }

        // 3. Falls der Nutzer ein Thumbnail ausgewählt hat, dieses Bild markieren
        thumbnailId?.let { thumbId ->
            imageService.markAsThumbnail(id, thumbId)
        }

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

    @GetMapping("/{itemId}/images/{imageId}/data")
    fun getImageData(
        @PathVariable itemId: Long,
        @PathVariable imageId: Long
    ): ResponseEntity<ByteArray> {
        // Hole alle Bilder für das Item und suche das richtige heraus
        val image = imageService.listImagesForItem(itemId)
            .find { it.id == imageId }
            ?: throw IllegalArgumentException("Bild mit ID $imageId für Item $itemId nicht gefunden.")

        // Gebe das Bild-Byte-Array mit dem korrekten Content-Type zurück
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, image.contentType)
            .body(image.data)
    }
}
