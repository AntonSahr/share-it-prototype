package de.shareit.shareitcore.ui.dto

import java.time.Instant

data class ImageDto(
    val id: Long,
    val filename: String,
    val contentType: String,
    val size: Long,
    val isThumbnail: Boolean,
    val uploadedAt: Instant
)