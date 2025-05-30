package de.shareit.shareitcore.domain.service

import de.shareit.shareitcore.domain.model.Item
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<Item, Long>