package de.shareit.shareitcore.infrastructure.adapter
import de.shareit.shareitcore.application.service.ImageService
import de.shareit.shareitcore.ui.dto.ImageDto
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/items/{itemId}/images")
class ImageController(
    private val imageService: ImageService
) {

    /**
     * 1. Mehrere Bilder uploaden (multipart/form-data)
     *    POST /api/items/{itemId}/images
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImages(
        @PathVariable itemId: Long,
        @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<List<ImageDto>> {
        val savedImages = imageService.uploadImages(itemId, files)
        // DTO für die Antwort (ohne Raw-Bytes)
        val dtos = savedImages.map { img ->
            ImageDto(
                id = img.id,
                filename = img.filename,
                contentType = img.contentType,
                size = img.size,
                isThumbnail = img.isThumbnail,
                uploadedAt = img.uploadedAt
            )
        }
        return ResponseEntity.ok(dtos)
    }

    /**
     * 2. Liste aller Bilder‐Metadaten für ein Item
     *    GET /api/items/{itemId}/images
     */
    @GetMapping
    fun listImages(
        @PathVariable itemId: Long
    ): ResponseEntity<List<ImageDto>> {
        val images = imageService.listImagesForItem(itemId)
        val dtos = images.map { img ->
            ImageDto(
                id = img.id,
                filename = img.filename,
                contentType = img.contentType,
                size = img.size,
                isThumbnail = img.isThumbnail,
                uploadedAt = img.uploadedAt
            )
        }
        return ResponseEntity.ok(dtos)
    }

    /**
     * 3. Ein einzelnes Bild‐Bytearray (Streaming)
     *    GET /api/items/{itemId}/images/{imageId}/data
     */
    @GetMapping("/{imageId}/data")
    fun getImageData(
        @PathVariable itemId: Long,
        @PathVariable imageId: Long
    ): ResponseEntity<ByteArray> {
        val data = imageService.getImageData(itemId, imageId)
        val imageEntity = imageService.listImagesForItem(itemId).find { it.id == imageId }
            ?: throw IllegalArgumentException("Bild nicht gefunden")

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, imageEntity.contentType)
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
    ): ResponseEntity<ImageDto> {
        val updatedImage = imageService.markAsThumbnail(itemId, imageId)
        val dto = ImageDto(
            id = updatedImage.id,
            filename = updatedImage.filename,
            contentType = updatedImage.contentType,
            size = updatedImage.size,
            isThumbnail = updatedImage.isThumbnail,
            uploadedAt = updatedImage.uploadedAt
        )
        return ResponseEntity.ok(dto)
    }
}

