package de.shareit.shareitcore.application.service.search

import java.math.BigDecimal

data class ItemSearchParams(
    val keyword: String? = null,
    val categoryId: Long? = null,
    val address: String? = null,
    val radiusKm: Double? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
)