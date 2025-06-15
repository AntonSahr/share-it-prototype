package de.shareit.shareitcore.infrastructure.adapter

import de.shareit.shareitcore.application.service.ImageService
import de.shareit.shareitcore.domain.model.ImageEntity
import de.shareit.shareitcore.ui.dto.ImageDto
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
@RequestMapping("/api/items/{itemId}/images")
class ImageRestController(
    private val imageService: ImageService
) {

    /**
     * 1. Upload mehrerer Bilder
     *    POST /api/items/{itemId}/images
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImages(
        @PathVariable itemId: Long,
        @RequestParam("files") files: List<MultipartFile>
    ): List<ImageDto> =
        imageService.uploadImages(itemId, files)
            .map { img -> img.toDto() }

    /**
     * 2. Liste aller Bild-Metadaten
     *    GET /api/items/{itemId}/images
     */
    @GetMapping
    fun listImages(@PathVariable itemId: Long): List<ImageDto> =
        imageService.listImagesForItem(itemId)
            .map { img -> img.toDto() }

    /**
     * 3. Stream eines Bild-Bytearrays
     *    GET /api/items/{itemId}/images/{imageId}/data
     */
    @GetMapping("/{imageId}/data")
    fun getImageData(
        @PathVariable itemId: Long,
        @PathVariable imageId: Long
    ): ResponseEntity<ByteArray> {
        val data = imageService.getImageData(itemId, imageId)
        val img = imageService.listImagesForItem(itemId)
            .find { it.id == imageId }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Bild nicht gefunden")

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, img.contentType)
            .body(data)
    }

    /**
     * 4. Ein Bild als Thumbnail markieren
     *    PATCH /api/items/{itemId}/images/{imageId}/thumbnail
     */
    @PatchMapping("/{imageId}/thumbnail")
    fun markThumbnail(
        @PathVariable itemId: Long,
        @PathVariable imageId: Long
    ): ImageDto {
        val updated = imageService.markAsThumbnail(itemId, imageId)
        return updated.toDto()
    }

    // Hilfsmethode: Entity -> DTO
    private fun ImageEntity.toDto() = ImageDto(
        id = this.id,
        filename = this.filename,
        contentType = this.contentType,
        size = this.size,
        isThumbnail = this.isThumbnail,
        uploadedAt = this.uploadedAt
    )
}
