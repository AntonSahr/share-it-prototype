package de.shareit.shareitcore.infrastructure.adapter

import de.shareit.shareitcore.application.service.GeocodingService
import de.shareit.shareitcore.application.service.search.SearchService
import de.shareit.shareitcore.application.service.search.ItemSearchParams
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import de.shareit.shareitcore.ui.dto.toResponseDto
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
@RequestMapping("/api/search")
class SearchRestController(
    private val searchService: SearchService,
    private val geocodingService: GeocodingService
) {

    @GetMapping
    fun search(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) address: String?,
        @RequestParam(required = false) radiusKm: Double?,
        @RequestParam(required = false) latitude: BigDecimal?,
        @RequestParam(required = false) longitude: BigDecimal?
    ): List<ItemResponseDto> {
        val params = ItemSearchParams(
            keyword = keyword,
            categoryId = categoryId,
            address = address,
            radiusKm = radiusKm
        )
        if (!address.isNullOrBlank()) {
            geocodingService.geocodeAddress(address)?.let { (lat, lon) ->
                params.latitude = lat
                params.longitude = lon
            }
        }
        return searchService.search(params)
            .map { it.toResponseDto() }
    }
}