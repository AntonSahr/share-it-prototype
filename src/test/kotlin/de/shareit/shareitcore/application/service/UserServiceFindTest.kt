package de.shareit.shareitcore.application.service

import de.shareit.shareitcore.application.UserService
import de.shareit.shareitcore.domain.model.AppUser
import de.shareit.shareitcore.domain.service.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class UserServiceFindTest {

    @Mock
    private lateinit var userRepo: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun `findEmailByProvider liefert E-Mail wenn User existiert`() {
        val provider = "github"
        val providerId = "1234"
        val user = AppUser(
            oauthProvider = provider,
            providerId = providerId,
            email = "max@example.com",
            displayName = "Max"
        )
        `when`(userRepo.findByOauthProviderAndProviderId(provider, providerId)).thenReturn(user)

        val result = userService.findEmailByProvider(provider, providerId)

        assertEquals("max@example.com", result)
        verify(userRepo).findByOauthProviderAndProviderId(provider, providerId)
    }

    @Test
    fun `findEmailByProvider liefert null wenn User nicht existiert`() {
        val provider = "github"
        val providerId = "5678"
        `when`(userRepo.findByOauthProviderAndProviderId(provider, providerId)).thenReturn(null)

        val result = userService.findEmailByProvider(provider, providerId)

        assertNull(result)
        verify(userRepo).findByOauthProviderAndProviderId(provider, providerId)
    }

    @Test
    fun `findDisplayNameByProvider liefert DisplayName wenn User existiert`() {
        val provider = "google"
        val providerId = "abcd"
        val user = AppUser(
            oauthProvider = provider,
            providerId = providerId,
            email = "anna@example.com",
            displayName = "Anna"
        )
        `when`(userRepo.findByOauthProviderAndProviderId(provider, providerId)).thenReturn(user)

        val result = userService.findDisplayNameByProvider(provider, providerId)

        assertEquals("Anna", result)
        verify(userRepo).findByOauthProviderAndProviderId(provider, providerId)
    }

    @Test
    fun `findDisplayNameByProvider liefert null wenn User nicht existiert`() {
        val provider = "google"
        val providerId = "wxyz"
        `when`(userRepo.findByOauthProviderAndProviderId(provider, providerId)).thenReturn(null)

        val result = userService.findDisplayNameByProvider(provider, providerId)

        assertNull(result)
        verify(userRepo).findByOauthProviderAndProviderId(provider, providerId)
    }
}
