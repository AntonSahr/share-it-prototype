package de.shareit.shareitcore.application


import de.shareit.shareitcore.domain.model.Category
import de.shareit.shareitcore.domain.service.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    /**
     * Erzeugt eine neue Kategorie mit optionaler parentId.
     * Wenn parentId angegeben ist, wird geprüft, ob diese existiert.
     */
    @Transactional
    fun createCategory(name: String, parentId: Long?): Category {
        val parentCategory: Category? = parentId?.let {
            categoryRepository.findById(it)
                .orElseThrow { IllegalArgumentException("Eltern-Kategorie mit ID $it nicht gefunden") }
        }
        val category = Category(name = name, parent = parentCategory)
        return categoryRepository.save(category)
    }


    /** Liefert alle Kategorien zurück (flache Liste). */
    @Transactional(readOnly = true)
    fun getAllCategories(): List<Category> = categoryRepository.findAll()

    /** Liefert eine Kategorie per ID oder wirft IllegalArgumentException, falls nicht gefunden. */
    @Transactional(readOnly = true)
    fun getCategoryById(id: Long): Category =
        categoryRepository.findById(id).orElseThrow { IllegalArgumentException("Kategorie mit ID $id nicht gefunden") }
}
