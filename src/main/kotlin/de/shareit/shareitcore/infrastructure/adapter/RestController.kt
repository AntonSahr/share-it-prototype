package de.shareit.shareitcore.infrastructure.adapter

import de.shareit.shareitcore.application.service.ListingService

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = ["http://localhost:5173"])
class ItemController(
    private val listingService: ListingService,
) {

    @GetMapping
    fun findAll() = listingService.findAll()

}



