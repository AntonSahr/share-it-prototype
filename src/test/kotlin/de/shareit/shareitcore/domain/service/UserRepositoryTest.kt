package de.shareit.shareitcore.domain.service

import de.shareit.shareitcore.domain.model.AppUser
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.Instant

@DataJpaTest
class UserRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository
) {

    @Test
    fun `findByOauthProviderAndProviderId should return user if exists`() {
        // Arrange: lege einen AppUser an und speichere ihn
        val existing = AppUser(
            oauthProvider = "github",
            providerId = "12345",
            email = "test@example.com",
            displayName = "Tester"
        ).apply {
            roles = setOf("ROLE_USER")
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
        userRepository.save(existing)

        // Act: suche nach ihm
        val result = userRepository.findByOauthProviderAndProviderId("github", "12345")

        // Assert: Ergebnis ist nicht null und hat dieselbe E-Mail
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result?.email).isEqualTo("test@example.com")
    }

    @Test
    fun `findByOauthProviderAndProviderId should return null if no user exists`() {
        // Act: suche nach einem nicht existenten Kombi-Key
        val result = userRepository.findByOauthProviderAndProviderId("google", "does-not-exist")

        // Assert: Ergebnis ist null
        Assertions.assertThat(result).isNull()
    }

    @Test
    fun `save should persist user and assign ID`() {
        // Arrange
        val newUser = AppUser(
            oauthProvider = "google",
            providerId = "abc123",
            email = "alice@example.com",
            displayName = "Alice"
        )

        // Act
        val saved = userRepository.save(newUser)

        // Assert: gespeichertes Objekt hat eine nicht-null ID und korrekte Felder
        Assertions.assertThat(saved.id).isNotNull
        Assertions.assertThat(saved.oauthProvider).isEqualTo("google")
        Assertions.assertThat(saved.email).isEqualTo("alice@example.com")
    }
}