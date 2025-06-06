package de.shareit.shareitcore.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "items")
open class Item(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @Column(nullable = false)
    open var title: String,

    @Column(columnDefinition = "TEXT")
    open var description: String? = null,

    @Column(name = "price_amount", nullable = false, precision = 10, scale = 2)
    open var priceAmount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "price_unit", nullable = false)
    open var priceUnit: PriceUnit,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    open var owner: AppUser,

    @Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    open var updatedAt: Instant = Instant.now(),

    @Column(name = "latitude", precision = 10, scale = 7, nullable = true)
    var latitude: BigDecimal? = null,

    @Column(name = "longitude", precision = 10, scale = 7, nullable = true)
    var longitude: BigDecimal? = null,

    @Column(name = "address", length = 255, nullable = false)
    open var address: String?,

    @OneToMany(mappedBy = "item", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableList<ImageEntity> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")    // ← Neue Spalte für die Kategorie
    open var category: Category? = null, // ← Category-Relation
) {
    protected constructor() : this(
        id = null,
        title = "",
        description = null,
        priceAmount = BigDecimal.ZERO,
        priceUnit = PriceUnit.DAILY,
        owner = AppUser(),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        latitude = null,
        longitude = null,
        address = null,
        category = null,
    )
}

