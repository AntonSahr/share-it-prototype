package de.shareit.shareitcore

import com.ninjasquad.springmockk.MockkBean
import de.shareit.shareitcore.application.CategoryService
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/**
 * Security-Tests für CategoryController unter Verwendung von MockK statt @MockBean
 */
@ExtendWith(SpringExtension::class, MockKExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerSecurityTest {

    @BeforeEach
    fun setup() {
        // In relaxed mode, getAllCategories() liefert emptyList(), Unit-Funktionen werden entspannt behandelt
    }

        @Autowired
        private lateinit var mockMvc: MockMvc

        @MockkBean(relaxed = true)
        private lateinit var categoryService: CategoryService

        @Test
        fun `GET categories is permit all`() {
            mockMvc.perform(get("/categories"))
                .andExpect(status().isOk)
        }

        @Test
        fun `anonymous GET new is redirected to login`() {
            mockMvc.perform(get("/categories/new"))
                .andExpect(status().is3xxRedirection)
        }

        @Test
        @WithMockUser(roles = ["USER"])
        fun `authenticated user GET new is forbidden`() {
            mockMvc.perform(get("/categories/new"))
                .andExpect(status().isForbidden)
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `admin GET new is ok`() {
            mockMvc.perform(get("/categories/new"))
                .andExpect(status().isOk)
        }

        @Test
        fun `anonymous POST create is redirected to login`() {
            mockMvc.perform(
                post("/categories")
                    .with(csrf())
                    .param("name", "TestCategory")
            )
                .andExpect(status().is3xxRedirection)
        }

        @Test
        @WithMockUser(roles = ["USER"])
        fun `authenticated user POST create is forbidden`() {
            mockMvc.perform(
                post("/categories")
                    .with(csrf())
                    .param("name", "TestCategory")
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `admin POST create is redirected to categories list`() {
            mockMvc.perform(
                post("/categories")
                    .with(csrf())
                    .param("name", "TestCategory")
            )
                .andExpect(status().is3xxRedirection)
                .andExpect(redirectedUrl("/categories"))
        }
    }
