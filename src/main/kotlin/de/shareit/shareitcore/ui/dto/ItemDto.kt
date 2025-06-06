package de.shareit.shareitcore.web.dto

import de.shareit.shareitcore.domain.model.PriceUnit
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class ItemDto(
    @field:NotBlank(message = "Titel darf nicht leer sein")
    var title: String,

    var description: String? = null,

    @field:NotNull(message = "Preis darf nicht null sein")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Preis muss größer 0 sein")
    var priceAmount: BigDecimal,

    @field:NotNull(message = "Preiseinheit muss gesetzt sein")
    var priceUnit: PriceUnit,

    @field:NotNull(message = "Adresse darf nicht leer sein")
    var address: String ="",

    var latitude: BigDecimal? = null,
    var longitude: BigDecimal? = null,

    var categoryId: Long? = null,
)
