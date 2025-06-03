package de.shareit.shareitcore.domain.service

import de.shareit.shareitcore.domain.model.ImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageRepository : JpaRepository<ImageEntity, Long> {
    // Alle Bilder zu einem Item finden
    fun findAllByItemId(itemId: Long): List<ImageEntity>

    // Thumbnail (falls vorhanden) für ein Item suchen
    fun findByItemIdAndIsThumbnailTrue(itemId: Long): ImageEntity?
}