package de.shareit.shareitcore.application.service.search

import de.shareit.shareitcore.application.service.GeocodingService
import de.shareit.shareitcore.domain.model.Item
import de.shareit.shareitcore.domain.service.ItemRepository
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import de.shareit.shareitcore.ui.dto.toDto
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.math.*


@Service
class SearchService(
    private val itemRepository: ItemRepository,
    private val geocodingService: GeocodingService,
) {
    fun search(params: ItemSearchParams): List<Item> {

        val coordinates: Pair<BigDecimal, BigDecimal>? = when {
            params.latitude != null && params.longitude != null -> Pair(params.latitude, params.longitude)
            !params.address.isNullOrBlank() -> geocodingService.geocode(params.address)
            else -> null
        }

        val lat = coordinates?.first
        val lng = coordinates?.second

       val result = itemRepository
           .searchItems(
               keyword =  params.keyword,
               categoryId = params.categoryId,
               radius = params.radiusKm,
               lat = lat,
               lng = lng
           )

        return result
    }

    fun Item.toResponseDto(): ItemResponseDto {
        return ItemResponseDto(
            id = this.id!!,
            title = this.title,
            description = this.description,
            priceAmount = this.priceAmount,
            priceUnit = this.priceUnit,
            ownerId = this.owner.id!!,
            ownerDisplayName = this.owner.displayName,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            latitude = this.latitude,
            longitude = this.longitude,
            address = this.address,
            categoryId = this.category?.id,
            images = this.images.map { it.
            toDto() } // ImageEntity → ImageDto Mapping
        )
    }


}