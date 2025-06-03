package de.shareit.shareitcore.application.service

import de.shareit.shareitcore.application.UserService
import de.shareit.shareitcore.domain.model.AppUser
import de.shareit.shareitcore.domain.service.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class UserServiceUpdateTest {

    @Mock
    private lateinit var userRepo: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    @Captor
    private lateinit var userCaptor: ArgumentCaptor<AppUser>

    @Test
    fun `updateEmailForUser aktualisiert E-Mail wenn User existiert und E-Mail valide ist`() {
        val provider = "github"
        val providerId = "1234"
        val existingUser = AppUser(
            oauthProvider = provider,
            providerId = providerId,
            email = "old@example.com",
            displayName = "OldName"
        )
        `when`(userRepo.findByOauthProviderAndProviderId(provider, providerId)).thenReturn(existingUser)
        `when`(userRepo.save(any(AppUser::class.java))).thenAnswer { invocation -> invocation.arguments[0] }

        userService.updateEmailForUser(provider, providerId, "new@example.com")

        verify(userRepo).findByOauthProviderAndProviderId(provider, providerId)
        verify(userRepo).save(userCaptor.capture())
        val savedUser = userCaptor.value
        assertEquals("new@example.com", savedUser.email)
        assertEquals("OldName", savedUser.displayName)
    }

    @Test
    fun `updateEmailForUser wirft IllegalArgumentException wenn User nicht existiert`() {
        val provider = "github"
        val providerId = "no-user"
        `when`(userRepo.findByOauthProviderAndProviderId(provider, providerId)).thenReturn(null)

        val ex = assertThrows<IllegalArgumentException> {
            userService.updateEmailForUser(provider, providerId, "new@example.com")
        }
        assertEquals("Unbekannter User beim E-Mail-Update", ex.message)
        verify(userRepo).findByOauthProviderAndProviderId(provider, providerId)
        verify(userRepo, never()).save(any())
    }

    @Test
    fun `updateEmailForUser wirft IllegalArgumentException wenn E-Mail ungültig ist`() {
        val provider = "github"
        val providerId = "1234"
        val existingUser = AppUser(
            oauthProvider = provider,
            providerId = providerId,
            email = "old@example.com",
            displayName = "OldName"
        )
        `when`(userRepo.findByOauthProviderAndProviderId(provider, providerId)).thenReturn(existingUser)

        val ex = assertThrows<IllegalArgumentException> {
            userService.updateEmailForUser(provider, providerId, "invalid-email")
        }
        assertEquals("Ungültige E-Mail-Adresse", ex.message)
        verify(userRepo).findByOauthProviderAndProviderId(provider, providerId)
        verify(userRepo, never()).save(any())
    }

    @Test
    fun `updateDisplayNameForUser aktualisiert DisplayName wenn User existiert`() {
        val provider = "google"
        val providerId = "abcd"
        val existingUser = AppUser(
            oauthProvider = provider,
            providerId = providerId,
            email = "anna@example.com",
            displayName = "Anna"
        )
        `when`(userRepo.findByOauthProviderAndProviderId(provider, providerId)).thenReturn(existingUser)
        `when`(userRepo.save(any(AppUser::class.java))).thenAnswer { invocation -> invocation.arguments[0] }

        userService.updateDisplayNameForUser(provider, providerId, "Anya")

        verify(userRepo).findByOauthProviderAndProviderId(provider, providerId)
        verify(userRepo).save(userCaptor.capture())
        val savedUser = userCaptor.value
        assertEquals("Anya", savedUser.displayName)
        assertEquals("anna@example.com", savedUser.email)
    }

    @Test
    fun `updateDisplayNameForUser wirft IllegalArgumentException wenn User nicht existiert`() {
        val provider = "google"
        val providerId = "unknown"
        `when`(userRepo.findByOauthProviderAndProviderId(provider, providerId)).thenReturn(null)

        val ex = assertThrows<IllegalArgumentException> {
            userService.updateDisplayNameForUser(provider, providerId, "NewName")
        }
        assertEquals("Unbekannter User beim Namen-Update", ex.message)
        verify(userRepo).findByOauthProviderAndProviderId(provider, providerId)
        verify(userRepo, never()).save(any())
    }
}
