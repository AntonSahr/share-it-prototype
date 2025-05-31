package de.shareit.shareitcore.application
import de.shareit.shareitcore.domain.model.AppUser
import de.shareit.shareitcore.domain.service.UserRepository
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

        // Dynamisch den key holen
        val userNameAttributeName = request
            .clientRegistration
            .providerDetails
            .userInfoEndpoint
            .userNameAttributeName

        // providerId aus genau diesem Attribut lesen
        val providerId = attributes[userNameAttributeName]
            ?.toString()
            ?: throw IllegalArgumentException("Unknown user id attribute '$userNameAttributeName'")

        // Email (kann für GitHub null sein!)
        val email = attributes["email"] as? String

        // Wenn Du E-Mail zwingend brauchst, wirf eine Exception oder hol sie nach:
        // if (email == null && request.clientRegistration.registrationId == "github") {
        //   // Call /user/emails API nach
        // }

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

        // Hier als dritten Parameter den dynamischen key verwenden!
        return DefaultOAuth2User(
            authorities,
            attributes,
            userNameAttributeName
        )
    }

}
