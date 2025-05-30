package de.shareit.shareitcore.infrastructure.adapter

import de.shareit.shareitcore.domain.model.Item
import de.shareit.shareitcore.ui.ItemDto
import de.shareit.shareitcore.domain.service.ItemRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/items")
class ItemController(private val repo: ItemRepository) {

    @GetMapping
    fun findAll() = repo.findAll()

    @PostMapping
    fun create(@RequestBody dto: ItemDto): Item {
        val item = Item(
            title = dto.title,
            description = dto.description,
            price = dto.price
        )
        return repo.save(item)
    }
}



