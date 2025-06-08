package de.shareit.shareitcore.ui


import de.shareit.shareitcore.application.CategoryService
import de.shareit.shareitcore.domain.model.Category
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    /**
     * Listet alle vorhandenen Kategorien (optional: Baum-Ansicht)
     * und zeigt sie auf /categories
     */
    @GetMapping
    fun listAll(model: Model): String {
        val categories: List<Category> = categoryService.getAllCategories()
        model.addAttribute("categories", categories)
        return "categories/list"   // referenziert src/main/resources/templates/categories/list.html
    }

    /**
     * GET /categories/new – Formular, um eine neue Kategorie anzulegen.
     * Wir übergeben ein leeres Category-Objekt als Binding-Target und die schon existierenden Kategorien
     * als Auswahlmöglichkeit für parent.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    fun showCreateForm(model: Model): String {
        model.addAttribute("category", Category(name = "", parent = null))
        model.addAttribute("allCategories", categoryService.getAllCategories())
        return "categories/new"    // referenziert src/main/resources/templates/categories/new.html
    }

    /**
     * POST /categories – speichert eine neue Kategorie.
     * Validierung: Name darf nicht leer sein. Parent darf entweder leer sein oder auf eine existierende ID zeigen.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    fun createCategory(
        @Valid @ModelAttribute("category") category: Category,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            // Wenn Validierungsfehler auftreten, Formular erneut anzeigen
            model.addAttribute("allCategories", categoryService.getAllCategories())
            return "categories/new"
        }

        // category.parent!!.id enthält die parentId (über das Select-Feld im Formular)
        val parentId: Long? = category.parent?.id
        categoryService.createCategory(category.name, parentId)
        return "redirect:/categories"
    }
}
