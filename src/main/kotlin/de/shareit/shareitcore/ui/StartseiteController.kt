package de.shareit.shareitcore.ui

import de.shareit.shareitcore.application.ListingService
import de.shareit.shareitcore.domain.model.Item
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.collections.map

@Controller
class StartseiteController(
    private val listingService: ListingService
) {

    @GetMapping("/")
    fun showHomePage(model: Model): String {
        // Rufe alle Items ab (ohne Suche)
        val items = listingService.findAll()

        // In DTOs umwandeln, falls dein Template damit rechnet
        val dtoList = items.map { item ->
            ItemResponseDto(
                id          = item.id!!,
                title       = item.title,
                description = item.description,
                priceAmount = item.priceAmount,
                priceUnit   = item.priceUnit,
                address     = item.address,
                latitude = item.latitude,
                longitude = item.longitude,
                ownerId = item.ownerId,
                ownerDisplayName = item.ownerDisplayName,
                updatedAt = item.updatedAt,
                createdAt  = item.createdAt,
            )
        }

        model.addAttribute("itemList", dtoList)
        return "index"    // Zeigt resources/templates/index.html an
    }
}