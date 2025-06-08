package de.shareit.shareitcore.domain.service

import de.shareit.shareitcore.domain.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
}