package de.shareit.shareitcore.application.service

import de.shareit.shareitcore.application.UserService
import de.shareit.shareitcore.domain.model.AppUser
import de.shareit.shareitcore.domain.service.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepo: UserRepository

    @Mock
    private lateinit var delegateService: DefaultOAuth2UserService

    @InjectMocks
    private lateinit var userService: UserService

    private lateinit var oauthRequest: OAuth2UserRequest
    private lateinit var validOAuth2User: OAuth2User

    @BeforeEach
    fun setUp() {
        // 1. Erzeuge ein Fake-OAuth2User (z.B. von Google) mit Attributen "sub", "email", "name"
        val attributes = mapOf(
            "sub" to "google-123",
            "email" to "max@example.com",
            "name" to "Max Mustermann"
        )
        validOAuth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "sub" // "sub" ist der Name-Attribute-Key
        )

        // 2. Erstelle eine ClientRegistration für "google", mit nameAttribute="sub"
        val registration = ClientRegistration.withRegistrationId("google")
            .clientId("dummy-client-id")
            .clientSecret("dummy-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "profile", "email")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://www.googleapis.com/oauth2/v4/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName("sub")
            .clientName("Google")
            .build()

        // 3. Simuliere einen OAuth2AccessToken (dummy)
        val accessToken = OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "dummy-token",
            java.time.Instant.now(),
            java.time.Instant.now().plusSeconds(3600)
        )

        // 4. Baue den OAuth2UserRequest
        oauthRequest = OAuth2UserRequest(registration, accessToken)

        // 5. Tausche das interne DefaultOAuth2UserService-Objekt auf unseren Mock aus (per Reflection)
        val field = UserService::class.java.getDeclaredField("delegate")
        field.isAccessible = true
        field.set(userService, delegateService)
    }

    @Test
    fun `loadUser should return existing user with correct authorities without saving`() {
        // Arrange
        // 1. Delegate liefert unser validOAuth2User
        whenever(delegateService.loadUser(any<OAuth2UserRequest>())).doReturn(validOAuth2User)

        // 2. Simuliere, dass dieser User bereits in der DB existiert
        val existing = AppUser(
            oauthProvider = "google",
            providerId = "google-123",
            email = "max@example.com",
            displayName = "Max Mustermann"
        ).apply {
            id = 1L
            roles = setOf("ROLE_USER", "ROLE_ADMIN")
        }
        whenever(userRepo.findByOauthProviderAndProviderId("google", "google-123")).doReturn(existing)

        // Act
        val resultUser = userService.loadUser(oauthRequest)

        // Assert
        val granted = resultUser.authorities.map { it.authority }.toSet()
        assertThat(granted).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")

        // attributes müssen identisch mit validOAuth2User sein
        assertThat(resultUser.attributes["email"]).isEqualTo("max@example.com")
        assertThat(resultUser.attributes["name"]).isEqualTo("Max Mustermann")

        // save darf hier nicht aufgerufen werden, weil Nutzer schon existierte
        verify(userRepo, times(0)).save(any())
    }

    @Test
    fun `loadUser should create new user if not exists and assign ROLE_USER`() {
        // Arrange
        whenever(delegateService.loadUser(any<OAuth2UserRequest>())).doReturn(validOAuth2User)

        // 1. DB sagt null, das heißt: Nutzer existiert noch nicht
        whenever(userRepo.findByOauthProviderAndProviderId("google", "google-123")).doReturn(null)

        // 2. Wenn save aufgerufen wird, gebe ein gespeichertes AppUser-Objekt zurück
        val saved = AppUser(
            oauthProvider = "google",
            providerId = "google-123",
            email = "max@example.com",
            displayName = "Max Mustermann"
        ).apply {
            id = 42L
            roles = setOf("ROLE_USER")
        }
        whenever(userRepo.save(any())).doReturn(saved)

        // Act
        val resultUser = userService.loadUser(oauthRequest)

        // Assert
        // a) Es wurde genau 1 x save() aufgerufen
        verify(userRepo, times(1)).save(any())

        // b) Rollen aus dem gespeicherten AppUser-Objekt
        val authorities = resultUser.authorities.map { it.authority }
        assertThat(authorities).containsExactly("ROLE_USER")

        // c) Attribute weiterhin korrekt vorhanden
        assertThat(resultUser.attributes["email"]).isEqualTo("max@example.com")
    }

    @Test
    fun `findEmailByProvider returns email when user exists`() {
        // Arrange
        val appUser = AppUser(
            oauthProvider = "github",
            providerId = "gh-007",
            email = "james.bond@example.com",
            displayName = "James Bond"
        )
        whenever(userRepo.findByOauthProviderAndProviderId("github", "gh-007"))
            .doReturn(appUser)

        // Act
        val email = userService.findEmailByProvider("github", "gh-007")

        // Assert
        assertThat(email).isEqualTo("james.bond@example.com")
    }

    @Test
    fun `findEmailByProvider returns null when user does not exist`() {
        // Arrange
        whenever(userRepo.findByOauthProviderAndProviderId("github", "gh-008"))
            .doReturn(null)

        // Act
        val email = userService.findEmailByProvider("github", "gh-008")

        // Assert
        assertThat(email).isNull()
    }

    @Test
    fun `updateEmailForUser successfully updates email`() {
        // Arrange
        val userEntity = AppUser(
            oauthProvider = "google",
            providerId = "google-123",
            email = "old@example.com",
            displayName = "Max"
        ).apply { id = 5L }
        whenever(userRepo.findByOauthProviderAndProviderId("google", "google-123"))
            .doReturn(userEntity)

        // Act
        userService.updateEmailForUser("google", "google-123", "new@example.com")

        // Assert
        // a) userEntity.email wurde geändert
        assertThat(userEntity.email).isEqualTo("new@example.com")

        // b) save() wurde aufgerufen
        verify(userRepo, times(1)).save(userEntity)
    }

    @Test
    fun `updateEmailForUser throws on invalid email`() {
        // Arrange
        val userEntity = AppUser(
            oauthProvider = "google",
            providerId = "google-123",
            email = "old@example.com",
            displayName = "Max"
        )
        whenever(userRepo.findByOauthProviderAndProviderId("google", "google-123"))
            .doReturn(userEntity)

        // Act & Assert
        val ex = assertThrows<IllegalArgumentException> {
            userService.updateEmailForUser("google", "google-123", "invalid-email-format")
        }
        assertThat(ex.message).isEqualTo("Ungültige E-Mail-Adresse")
    }

    @Test
    fun `updateEmailForUser throws when user not found`() {
        // Arrange
        whenever(userRepo.findByOauthProviderAndProviderId("gitlab", "gl-001"))
            .doReturn(null)

        // Act & Assert
        val ex = assertThrows<IllegalArgumentException> {
            userService.updateEmailForUser("gitlab", "gl-001", "test@example.com")
        }
        assertThat(ex.message).isEqualTo("Unbekannter User beim E-Mail-Update")
    }

    @Test
    fun `findDisplayNameByProvider returns displayName when user exists`() {
        // Arrange
        val userEntity = AppUser(
            oauthProvider = "github",
            providerId = "gh-xyz",
            email = "alice@example.com",
            displayName = "Alice Wonderland"
        )
        whenever(userRepo.findByOauthProviderAndProviderId("github", "gh-xyz"))
            .doReturn(userEntity)

        // Act
        val displayName = userService.findDisplayNameByProvider("github", "gh-xyz")

        // Assert
        assertThat(displayName).isEqualTo("Alice Wonderland")
    }

    @Test
    fun `findDisplayNameByProvider returns null when user does not exist`() {
        // Arrange
        whenever(userRepo.findByOauthProviderAndProviderId("github", "gh-abc"))
            .doReturn(null)

        // Act
        val displayName = userService.findDisplayNameByProvider("github", "gh-abc")

        // Assert
        assertThat(displayName).isNull()
    }

    @Test
    fun `updateDisplayNameForUser successfully updates displayName`() {
        // Arrange
        val userEntity = AppUser(
            oauthProvider = "google",
            providerId = "google-789",
            email = "bob@example.com",
            displayName = "Bob Old"
        ).apply { id = 9L }
        whenever(userRepo.findByOauthProviderAndProviderId("google", "google-789"))
            .doReturn(userEntity)

        // Act
        userService.updateDisplayNameForUser("google", "google-789", "Bob New")

        // Assert
        // a) displayName wurde angepasst
        assertThat(userEntity.displayName).isEqualTo("Bob New")

        // b) save() wurde aufgerufen
        verify(userRepo, times(1)).save(userEntity)
    }

    @Test
    fun `updateDisplayNameForUser throws when user not found`() {
        // Arrange
        whenever(userRepo.findByOauthProviderAndProviderId("gitlab", "gl-002"))
            .doReturn(null)

        // Act & Assert
        val ex = assertThrows<IllegalArgumentException> {
            userService.updateDisplayNameForUser("gitlab", "gl-002", "Test Name")
        }
        assertThat(ex.message).isEqualTo("Unbekannter User beim Namen-Update")
    }
}
