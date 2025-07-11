package de.shareit.shareitcore.ui

import de.shareit.shareitcore.application.service.CategoryService
import de.shareit.shareitcore.application.service.GeocodingService
import de.shareit.shareitcore.application.service.search.ItemSearchParams
import de.shareit.shareitcore.application.service.search.SearchService
import de.shareit.shareitcore.ui.dto.toResponseDto
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import org.apache.tomcat.jni.Buffer.address
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal

@Controller
@RequestMapping("/search")
class SearchController(
    private val searchService: SearchService,
    private val categoryService: CategoryService,
    private val geocodingService: GeocodingService,
) {

    @GetMapping
    fun search(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) address: String?,
        @RequestParam(required = false) radiusKm: Double?,
        @RequestParam(required = false) latitude: BigDecimal?,
        @RequestParam(required = false) longitude: BigDecimal?,
        model: Model
    ): String {
        val params = ItemSearchParams(
            keyword = keyword,
            categoryId = categoryId,
            address = address,
            radiusKm = radiusKm
        )

        if (!address.isNullOrBlank()) {
            val location = geocodingService.geocodeAddress(address)
            if (location != null) {
                params.latitude = location.first
                params.longitude = location.second
            }
        }

        val results: List<ItemResponseDto> = searchService.search(params).map { it.toResponseDto() }
        model.addAttribute("items", results)
        model.addAttribute("params", params)
        model.addAttribute("categories", categoryService.getAllCategories())
        return "search"
    }
}
