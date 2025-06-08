package de.shareit.shareitcore.application.service

import de.shareit.shareitcore.domain.model.Item
import de.shareit.shareitcore.domain.service.ItemRepository
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val itemRepository: ItemRepository
) {
    fun search(categoryId: Long, lat: Double, lng: Double, radiusKm: Double): List<Item> {
        val radiusMeters = radiusKm * 1000
        return itemRepository.findByCategoryAndLocationRadius(categoryId, lat, lng, radiusMeters)
    }
}
