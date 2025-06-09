package de.shareit.shareitcore.application.service
import de.shareit.shareitcore.domain.model.AppUser
import de.shareit.shareitcore.domain.service.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service


// 2) Der CustomOAuth2UserService
@Service
open class UserService(
    private val userRepo: UserRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val delegate = DefaultOAuth2UserService()


    override fun loadUser(request: OAuth2UserRequest): OAuth2User {
        val oauth2User = delegate.loadUser(request)
        val attributes = oauth2User.attributes

        val registrationId = request.clientRegistration.registrationId

        val userNameAttributeName = request
            .clientRegistration
            .providerDetails
            .userInfoEndpoint
            .userNameAttributeName

        val providerId = attributes[userNameAttributeName]
            ?.toString()
            ?: throw IllegalArgumentException("Unknown user id attribute '$userNameAttributeName'")

        val email = attributes["email"] as? String

        val user = userRepo
            .findByOauthProviderAndProviderId(registrationId, providerId)
            ?: userRepo.save(
                AppUser(
                    oauthProvider = registrationId,
                    providerId    = providerId,
                    email         = email ?: "",
                    displayName   = attributes["name"] as? String
                )
            )

        val authorities = user.roles.map(::SimpleGrantedAuthority)

        return DefaultOAuth2User(
            authorities,
            attributes,
            userNameAttributeName
        )
    }

    fun findEmailByProvider(provider: String, providerId: String): String? {
        return userRepo
            .findByOauthProviderAndProviderId(provider, providerId)
            ?.email
    }

    @Transactional
    fun updateEmailForUser(provider: String, providerId: String, newEmail: String) {
        val user = userRepo
            .findByOauthProviderAndProviderId(provider, providerId)
            ?: throw IllegalArgumentException("Unbekannter User beim E-Mail-Update")
        if (!newEmail.contains("@")) {
            throw IllegalArgumentException("Ungültige E-Mail-Adresse")
        }
        user.email = newEmail
        userRepo.save(user)
    }

    fun findDisplayNameByProvider(provider: String, providerId: String): String? {
        return userRepo
            .findByOauthProviderAndProviderId(provider, providerId)
            ?.displayName
    }

    @Transactional
    fun updateDisplayNameForUser(provider: String, providerId: String, newDisplayName: String) {
        val user = userRepo
            .findByOauthProviderAndProviderId(provider, providerId)
            ?: throw IllegalArgumentException("Unbekannter User beim Namen-Update")
        user.displayName = newDisplayName
        userRepo.save(user)
    }
}
