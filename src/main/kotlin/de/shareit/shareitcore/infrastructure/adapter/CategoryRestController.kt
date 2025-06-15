package de.shareit.shareitcore.infrastructure.adapter


import de.shareit.shareitcore.application.service.CategoryService
import de.shareit.shareitcore.domain.model.Category
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

data class CategoryDto(
    val id: Long,
    val name: String,
    val parentId: Long?
)

data class CreateCategoryRequest(
    @field:Valid
    val name: String,
    val parentId: Long? = null
)

@CrossOrigin(origins = ["http://localhost:5175"])
@RestController
@RequestMapping("/api/categories")
class CategoryRestController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getAll(): List<CategoryDto> =
        categoryService.getAllCategories().map(::toDto)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): CategoryDto =
        toDto(categoryService.getCategoryById(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@RequestBody @Valid req: CreateCategoryRequest): CategoryDto {
        val created: Category = categoryService.createCategory(req.name, req.parentId)
        return toDto(created)
    }

    private fun toDto(c: Category) = CategoryDto(
        id = c.id!!,
        name = c.name,
        parentId = c.parent?.id
    )
}
