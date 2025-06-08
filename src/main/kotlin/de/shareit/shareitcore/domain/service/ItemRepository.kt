package de.shareit.shareitcore.domain.service

import de.shareit.shareitcore.domain.model.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ItemRepository : JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    fun findByOwnerId(ownerId: Long): List<Item>

    @Query("""
    SELECT * FROM items 
    WHERE (:keyword IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:categoryId IS NULL OR category_id = :categoryId)
      AND (
        (:lat IS NULL OR :lng IS NULL OR :radius IS NULL) OR (
            latitude IS NOT NULL AND longitude IS NOT NULL AND
            6371000 * acos(
              cos(radians(:lat)) * cos(radians(latitude)) * 
              cos(radians(longitude) - radians(:lng)) +
              sin(radians(:lat)) * sin(radians(latitude))
            ) <= :radius
        )
    )
""", nativeQuery = true)
    fun searchItems(
        keyword: String?,
        categoryId: Long?,
        lat: BigDecimal?,
        lng: BigDecimal?,
        radius: Double?
    ): List<Item>

}