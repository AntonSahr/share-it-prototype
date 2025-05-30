package de.shareit.shareitcore.ui

import de.shareit.shareitcore.domain.model.Item
import java.math.BigDecimal

data class ItemDto(
    var title: String,
    var description: String?,
    var price: BigDecimal?,


    )
fun ItemDto.toEntity(): Item =
    Item(
        title = this.title,
        description = this.description,
        price = this.price
    )