package de.shareit.shareitcore.ui.dto

import de.shareit.shareitcore.domain.model.PriceUnit
import java.math.BigDecimal
import java.time.Instant

data class ItemResponseDto(
    val id: Long,
    val title: String,
    val description: String?,
    val priceAmount: BigDecimal,
    val priceUnit: PriceUnit,
    val ownerId: Long,
    val ownerDisplayName: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
