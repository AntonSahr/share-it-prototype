package de.shareit.shareitcore.domain.model


import jakarta.persistence.*

@Entity
@Table(name = "categories")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    // Parent-Kategorie (falls NULL ⇒ Top-Level)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,

    // Liste der direkten Kind-Kategorien, Cascade ALL, damit untergeordnete Kategorien automatisch persistiert/gelöscht werden
    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true)
    var children: MutableList<Category> = mutableListOf(),

){

}