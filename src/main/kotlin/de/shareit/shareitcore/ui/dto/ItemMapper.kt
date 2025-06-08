package de.shareit.shareitcore.ui.dto

import de.shareit.shareitcore.domain.model.ImageEntity
import de.shareit.shareitcore.domain.model.Item

fun ImageEntity.toDto(): ImageDto {
    return ImageDto(
        id = this.id,
        filename = this.filename,
        contentType = this.contentType,
        size = this.size,
        isThumbnail = this.isThumbnail,
        uploadedAt = this.uploadedAt
    )
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
        images = this.images.map { it.toDto() } // ImageEntity → ImageDto Mapping
    )
}