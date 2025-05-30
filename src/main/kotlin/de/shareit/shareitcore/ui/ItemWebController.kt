package de.shareit.shareitcore.ui

import de.shareit.shareitcore.domain.service.ItemRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ItemWebController(val repo: ItemRepository) {

    @GetMapping("/items")
    fun list(model: Model): String {
        model.addAttribute("items", repo.findAll())
        return "items"               // Thymeleaf-Template items.html
    }

    @GetMapping("/items/new")
    fun newForm(model: Model): String {
        model.addAttribute("itemDto", ItemDto(
            title = "",
            description = "",
            price = null
        )
        )
        return "item-form"          // ein weiteres Template mit Formular
    }

    @PostMapping("/items")
    fun create(@ModelAttribute dto: ItemDto): String {
        repo.save(dto.toEntity())
        return "redirect:/items"
    }
}