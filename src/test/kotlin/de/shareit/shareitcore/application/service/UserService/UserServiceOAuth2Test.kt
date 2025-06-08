package de.shareit.shareitcore.application.service.UserService

import de.shareit.shareitcore.application.service.UserService
import de.shareit.shareitcore.domain.model.AppUser
import de.shareit.shareitcore.domain.service.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User

@ExtendWith(MockitoExtension::class)
internal class UserServiceOAuth2Test {

    @Mock
    private lateinit var userRepo: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    @Captor
    private lateinit var userCaptor: ArgumentCaptor<AppUser>

    // Gemeinsame Testdaten
    private val providerIdAttr = "id"
    private val providerIdValue = "1234"
    private val email = "test@example.com"
    private val name = "Test User"
    private val attributes: Map<String, Any> = mapOf(
        providerIdAttr to providerIdValue,
        "email" to email,
        "name" to name
    )

    // Mocks und Spies, die in jedem Test gebraucht werden
    private lateinit var clientRegistration: ClientRegistration
    private lateinit var providerDetails: ClientRegistration.ProviderDetails
    private lateinit var userInfoEndpoint: ClientRegistration.ProviderDetails.UserInfoEndpoint
    private lateinit var oauthRequest: OAuth2UserRequest
    private lateinit var spyDelegate: DefaultOAuth2UserService
    private lateinit var serviceWithSpy: UserService
    private lateinit var mockedDelegateUser: DefaultOAuth2User

    @BeforeEach
    fun setUp() {
        // 1) ClientRegistration stubben: registrationId und providerDetails.userInfoEndpoint.userNameAttributeName
        clientRegistration = mock(ClientRegistration::class.java).apply {
            `when`(registrationId).thenReturn("github")
        }

        providerDetails =
            mock(ClientRegistration.ProviderDetails::class.java)
        userInfoEndpoint =
            mock(ClientRegistration.ProviderDetails.UserInfoEndpoint::class.java)

        // Stubbe genau die Methoden, die UserService.loadUser(...) tatsächlich aufruft:
        `when`(userInfoEndpoint.userNameAttributeName).thenReturn(providerIdAttr)
        `when`(providerDetails.userInfoEndpoint).thenReturn(userInfoEndpoint)
        `when`(clientRegistration.providerDetails).thenReturn(providerDetails)

        // 2) OAuth2UserRequest stubben: nur getClientRegistration() wird gebraucht
        oauthRequest = mock(OAuth2UserRequest::class.java)
        `when`(oauthRequest.clientRegistration).thenReturn(clientRegistration)

        // 3) Dummy-OAuth2User für den Delegate
        mockedDelegateUser = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            providerIdAttr
        )

        // 4) Delegate als Spy und loadUser vollständig stubben
        spyDelegate = spy(DefaultOAuth2UserService())
        doReturn(mockedDelegateUser)
            .`when`(spyDelegate)
            .loadUser(any(OAuth2UserRequest::class.java))

        // 5) Ersetze in UserService das private Feld "delegate" durch unseren spyDelegate
        serviceWithSpy = UserService(userRepo).also {
            val field = UserService::class.java.getDeclaredField("delegate")
            field.isAccessible = true
            field.set(it, spyDelegate)
        }
    }

    @Test
    fun `loadUser bei vorhandenem User gibt denselben DefaultOAuth2User zurück`() {
        // Repository stubben: User existiert bereits
        `when`(userRepo.findByOauthProviderAndProviderId("github", providerIdValue)).thenReturn(
            AppUser(
                oauthProvider = "github",
                providerId = providerIdValue,
                email = email,
                displayName = name
            )
        )

        // Service-Methode aufrufen
        val result: OAuth2User = serviceWithSpy.loadUser(oauthRequest)

        // Assertions: Rückgabe = Dummy-OAuth2User, kein Speichern
        assertTrue(result is DefaultOAuth2User)
        @Suppress("UNCHECKED_CAST")
        val resultCast = result as DefaultOAuth2User
        assertEquals(attributes, resultCast.attributes)
        assertEquals(providerIdValue, resultCast.name)
        assertTrue(resultCast.authorities.contains(SimpleGrantedAuthority("ROLE_USER")))

        verify(spyDelegate).loadUser(oauthRequest)
        verify(userRepo).findByOauthProviderAndProviderId("github", providerIdValue)
        verify(userRepo, never()).save(any())
    }

    @Test
    fun `loadUser bei nicht vorhandenem User legt neuen AppUser an`() {
        // Repository stubben: Kein User gefunden → save muss aufgerufen werden
        `when`(userRepo.findByOauthProviderAndProviderId("github", providerIdValue))
            .thenReturn(null)
        `when`(userRepo.save(any(AppUser::class.java))).thenAnswer { it.arguments[0] as AppUser }

        // Service-Methode aufrufen
        val result: OAuth2User = serviceWithSpy.loadUser(oauthRequest)

        // Assertions: Dummy-OAuth2User zurück, und neuer AppUser gespeichert
        assertTrue(result is DefaultOAuth2User)
        @Suppress("UNCHECKED_CAST")
        val resultCast = result as DefaultOAuth2User
        assertEquals(attributes, resultCast.attributes)
        assertEquals(providerIdValue, resultCast.name)
        assertTrue(resultCast.authorities.contains(SimpleGrantedAuthority("ROLE_USER")))

        // Prüfen, dass save mit korrektem AppUser aufgerufen wurde
        verify(userRepo).findByOauthProviderAndProviderId("github", providerIdValue)
        verify(userRepo).save(userCaptor.capture())
        val saved = userCaptor.value
        assertEquals("github", saved.oauthProvider)
        assertEquals(providerIdValue, saved.providerId)
        assertEquals(email, saved.email)
        assertEquals(name, saved.displayName)
    }
}
