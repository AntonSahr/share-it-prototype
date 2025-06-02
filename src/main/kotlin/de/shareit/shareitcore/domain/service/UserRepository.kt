package de.shareit.shareitcore.domain.service

import de.shareit.shareitcore.domain.model.AppUser
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<AppUser, Long> {
    fun findByOauthProviderAndProviderId(provider: String, providerId: String): AppUser?
    fun findByEmail(email: String): AppUser?
}