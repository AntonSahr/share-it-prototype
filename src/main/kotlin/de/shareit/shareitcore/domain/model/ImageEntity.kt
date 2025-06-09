package de.shareit.shareitcore.domain.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "images")
data class ImageEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // Byte‐Daten des Bildes. In H2: BLOB; in Postgres: bytea
    @Column(name = "data", nullable = false, columnDefinition = "BYTEA")
    val data: ByteArray,

    @Column(name = "filename", nullable = false)
    val filename: String,

    @Column(name = "content_type", nullable = false)
    val contentType: String,

    // Optional: Dateigröße
    @Column(name = "size", nullable = false)
    val size: Long,

    // Thumbnail‐Flag: true = dieses Bild ist das Thumbnail für sein Item
    @Column(name = "is_thumbnail", nullable = false)
    var isThumbnail: Boolean = false,

    @Column(name = "uploaded_at", nullable = false)
    val uploadedAt: Instant = Instant.now(),

    // ManyToOne zum Item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    val item: Item
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageEntity

        if (id != other.id) return false
        if (size != other.size) return false
        if (isThumbnail != other.isThumbnail) return false
        if (!data.contentEquals(other.data)) return false
        if (filename != other.filename) return false
        if (contentType != other.contentType) return false
        if (uploadedAt != other.uploadedAt) return false
        if (item != other.item) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + isThumbnail.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + uploadedAt.hashCode()
        result = 31 * result + item.hashCode()
        return result
    }
}
