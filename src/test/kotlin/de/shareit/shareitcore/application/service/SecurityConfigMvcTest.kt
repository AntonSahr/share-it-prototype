package de.shareit.shareitcore.application.service


import de.shareit.shareitcore.application.SecurityConfig
import de.shareit.shareitcore.ui.ItemWebController
import de.shareit.shareitcore.ui.ProfileController
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [ItemWebController::class, ProfileController::class, SecurityConfig::class])
class SecurityConfigMvcTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    // Wir müssen alle Beans simulieren, die in der Web-Schicht injiziert werden
    @MockBean
    private lateinit var oauth2UserService: OAuth2UserService<*, OAuth2User>

    @Test
    fun `accessing OAuth authorization endpoint should be permitted`() {
        mockMvc.perform(get("/oauth2/authorization/github"))
            .andExpect(status().is3xxRedirection)       // Redirect zu GitHub
            .andExpect(redirectedUrlPattern("https://github.com/login/oauth/authorize*"))
    }

    @Test
    fun `accessing items unauthenticated should redirect to OAuth login`() {
        mockMvc.perform(get("/items"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("**/oauth2/authorization/github"))
        // je nach Default-Provider: hinter /** könnte auch google statt github stehen
    }

    @Test
    fun `accessing public root should be OK`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
    }
}
