package de.shareit.shareitcore.application.service

import de.shareit.shareitcore.domain.model.ImageEntity
import de.shareit.shareitcore.domain.service.ImageRepository
import de.shareit.shareitcore.domain.service.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

@Service
class ImageService(
    private val itemRepository: ItemRepository,
    private val imageRepository: ImageRepository
) {

    /**
     * Speichert mehrere MultipartFile unter einem Item,
     * ohne ein Thumbnail zu setzen (isThumbnail bleibt false).
     */
    @Transactional
    fun uploadImages(itemId: Long, files: List<MultipartFile>): List<ImageEntity> {
        val item = itemRepository.findById(itemId)
            .orElseThrow { IllegalArgumentException("Item mit ID $itemId nicht gefunden.") }

        val savedEntities = files.map { file ->
            // Validierung: nur Bilder zulassen
            val contentType = file.contentType ?: throw IllegalArgumentException("Content-Type fehlt")
            if (!contentType.startsWith("image/")) {
                throw IllegalArgumentException("Nur Bilddateien erlaubt (aktueller Typ: $contentType)")
            }

            // Optional: Max-Größe prüfen, z. B. 5 MB
            val maxSize = 5L * 1024 * 1024
            if (file.size > maxSize) {
                throw IllegalArgumentException("Datei ${file.originalFilename} ist zu groß (maximal 5 MB).")
            }

            val bytes = file.bytes
            ImageEntity(
                data = bytes,
                filename = file.originalFilename ?: "unknown.jpg",
                contentType = contentType,
                size = file.size,
                isThumbnail = false,
                uploadedAt = Instant.now(),
                item = item
            ).also { image ->
                item.images.add(image)
            }
        }

        // Speichern – JPA cascaded: Item wird aktualisiert, Images werden persistiert
        val updatedItem = itemRepository.save(item)
        return updatedItem.images.filter { it.id != 0L } // IDs gesetzt nach Save
    }

    /**
     * Setzt ein bestimmtes Bild (imageId) als Thumbnail für sein Item.
     * Hebt vorherige Thumbnail‐Markierung auf, falls vorhanden.
     */
    @Transactional
    fun markAsThumbnail(itemId: Long, imageId: Long): ImageEntity {
        // 1. Stelle sicher, dass das Bild zu diesem Item gehört
        val image = imageRepository.findById(imageId)
            .orElseThrow { IllegalArgumentException("Bild mit ID $imageId nicht gefunden.") }

        if (image.item.id != itemId) {
            throw IllegalArgumentException("Bild gehört nicht zu Item $itemId.")
        }

        // 2. Vorhandenes Thumbnail zurücksetzen (falls vorhanden)
        val existingThumb = imageRepository.findByItemIdAndIsThumbnailTrue(itemId)
        if (existingThumb != null && existingThumb.id != imageId) {
            existingThumb.isThumbnail = false
            imageRepository.save(existingThumb)
        }

        // 3. Dieses Bild als Thumbnail setzen
        image.isThumbnail = true
        return imageRepository.save(image)
    }

    /**
     * Holt alle Bilder eines Items (IDs und ggf. Thumbnail‐Flag).
     * Die Nutzdaten (ByteArray) holst du per separatem Endpunkt (Streaming).
     */
    @Transactional(readOnly = true)
    fun listImagesForItem(itemId: Long): List<ImageEntity> {
        return imageRepository.findAllByItemId(itemId)
    }

    /**
     * Bilddaten als ByteArray ausliefern (z. B. für GET /api/items/{itemId}/images/{imageId}/data).
     */
    @Transactional(readOnly = true)
    fun getImageData(itemId: Long, imageId: Long): ByteArray {
        val image = imageRepository.findById(imageId)
            .orElseThrow { IllegalArgumentException("Bild mit ID $imageId nicht gefunden.") }
        if (image.item.id != itemId) {
            throw IllegalArgumentException("Bild gehört nicht zu Item $itemId.")
        }
        return image.data
    }
}
