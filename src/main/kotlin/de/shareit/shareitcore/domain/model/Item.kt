package de.shareit.shareitcore.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
data class Item(
    @Id @GeneratedValue
    val id: Long = 0,
    val title: String,
    val description: String?,
    val price: BigDecimal?
)
