package de.shareit.shareitcore.application.service.search

import de.shareit.shareitcore.application.service.GeocodingService
import de.shareit.shareitcore.domain.model.Item
import de.shareit.shareitcore.domain.service.ItemRepository
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import de.shareit.shareitcore.ui.dto.toDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class SearchService(
    private val itemRepository: ItemRepository,
    private val geocodingService: GeocodingService,
) {
    private val logger = LoggerFactory.getLogger(SearchService::class.java)


    fun search(params: ItemSearchParams): List<Item> {
        logger.debug("Suchanfrage empfangen mit Parametern: {}", params)

        val coordinates = params.address?.takeIf { it.isNotBlank() }
            ?.let { address ->
                try {
                    val geo = geocodingService.geocodeAddress(address)
                    logger.debug("Geocoding erfolgreich: {} -> {}", address, geo)
                    geo
                } catch (e: Exception) {
                    logger.debug("Geocoding fehlgeschlagen für Adresse: $address", e)
                    null
                }
            }

        val lat = coordinates?.first
        val lng = coordinates?.second

        logger.debug("Latitude: {}, Longitude: {}", lat, lng)

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