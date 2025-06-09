package de.shareit.shareitcore.infrastructure.adapter

import de.shareit.shareitcore.application.ListingService
import de.shareit.shareitcore.domain.model.Item

import de.shareit.shareitcore.domain.service.ItemRepository
import de.shareit.shareitcore.web.dto.ItemDto
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = ["http://localhost:5173"])
class ItemRestController(
    private val listingService: ListingService,
) {

    @GetMapping
    fun findAll() = listingService.findAll()

}



