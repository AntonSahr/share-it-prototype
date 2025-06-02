package de.shareit.shareitcore.application

import de.shareit.shareitcore.domain.model.Item
import de.shareit.shareitcore.domain.service.ItemRepository
import de.shareit.shareitcore.domain.service.UserRepository
import de.shareit.shareitcore.ui.dto.ItemResponseDto
import de.shareit.shareitcore.web.dto.ItemDto
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ListingService(
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository,
    private val geocodingService: GeocodingService
) {
    /**
     * Neues Item für ownerId anlegen.
     * @throws IllegalArgumentException, wenn User nicht existiert.
     */
    @Transactional
    fun createItem(ownderId: Long, dto: ItemDto): ItemResponseDto{
        val owner = userRepository.findById(ownderId)
            .orElseThrow { RuntimeException("Owner mit ID $ownderId nicht gefunden") }

        val (lat, lon) = geocodingService.geocode(dto.address?: "")
            ?: Pair(null, null)

        val item = Item(
            title = dto.title,
            description = dto.description,
            priceAmount = dto.priceAmount,
            priceUnit = dto.priceUnit,
            owner = owner,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            longitude = lon,
            latitude = lat,
            address = dto.address,
        )

        val saved = itemRepository.save(item)
        return mapToResponseDto(saved)
    }

    /**
     * Item updaten – nur, wenn es existiert und ownerId stimmt.
     * @throws IllegalArgumentException, wenn Item nicht existiert oder nicht dem Owner gehört.
     */
    @Transactional
    open fun updateItem(ownerId: Long, itemId: Long, dto: ItemDto): ItemResponseDto {
        val item = itemRepository.findById(itemId)
            .orElseThrow { IllegalArgumentException("Item mit ID $itemId nicht gefunden") }

        if (item.owner.id != ownerId) {
            throw IllegalArgumentException("Nur der Owner kann dieses Item ändern")
        }

        if (dto.address.isNullOrBlank()) {
            val (lat, lon) = geocodingService.geocode(dto.address!!) ?: Pair(null, null)
            item.latitude = lat
            item.longitude = lon
            item.address = dto.address
        } else {
            item.latitude = null
            item.longitude = null
            item.address = null
        }

        item.title = dto.title
        item.description = dto.description
        item.priceAmount = dto.priceAmount
        item.priceUnit = dto.priceUnit
        item.updatedAt = Instant.now()

        val saved = itemRepository.save(item)
        return mapToResponseDto(saved)
    }

    /**
     * Alle Items (unabhängig vom Owner) abrufen.
     */
    open fun findAll(): List<ItemResponseDto> {
        return itemRepository.findAll().map(this::mapToResponseDto)
    }

    /**
     * Einzelnes Item nach ID.
     * @throws IllegalArgumentException, wenn nicht existiert.
     */
    open fun findById(itemId: Long): ItemResponseDto {
        val item = itemRepository.findById(itemId)
            .orElseThrow { IllegalArgumentException("Item mit ID $itemId nicht gefunden") }
        return mapToResponseDto(item)
    }

    /**
     * Alle Items eines bestimmten Users (Owner).
     * @throws IllegalArgumentException, wenn Owner nicht existiert.
     */
    open fun findByOwner(ownerId: Long): List<ItemResponseDto> {
        userRepository.findById(ownerId)
            .orElseThrow { IllegalArgumentException("Owner mit ID $ownerId nicht gefunden") }

        return itemRepository.findByOwnerId(ownerId)
            .map(this::mapToResponseDto)
    }

    /**
     * Item löschen – nur möglich, wenn Owner übereinstimmt.
     * @throws IllegalArgumentException wenn Item nicht gefunden oder Owner nicht stimmt.
     */
    @Transactional
    open fun deleteItem(ownerId: Long, itemId: Long) {
        val item = itemRepository.findById(itemId)
            .orElseThrow { IllegalArgumentException("Item mit ID $itemId nicht gefunden") }

        if (item.owner.id != ownerId) {
            throw IllegalArgumentException("Nur der Owner kann dieses Item löschen")
        }
        itemRepository.delete(item)
    }

    private fun mapToResponseDto(item: Item): ItemResponseDto {
        return ItemResponseDto(
            id = item.id!!,
            title = item.title,
            description = item.description,
            priceAmount = item.priceAmount,
            priceUnit = item.priceUnit,
            ownerId = item.owner.id!!,
            ownerDisplayName = item.owner.displayName,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt,
            longitude = item.longitude,
            latitude = item.latitude,
            address = item.address
        )
    }
}