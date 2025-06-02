package de.shareit.shareitcore.domain.model

import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.Instant

@Entity
open class AppUser(
    @Id @GeneratedValue
    open var id: Long? = null,

    // Authentifizierung
    open var oauthProvider: String = "",
    open var providerId: String = "",
    open var email: String,                    // eindeutig, zum Login und für Benachrichtigungen

    // Profil-Informationen
    open var displayName: String?,             // z.B. Vorname + Nachname oder Spitzname
    open var bio: String? = null,              // kurze Beschreibung

    // Autorisierung
    @ElementCollection(fetch = FetchType.EAGER)
    open var roles: Set<String> = setOf("ROLE_USER"),

    // Status-Felder
    open var enabled: Boolean = false,         // nach E-Mail-Verifizierung
    open var locked: Boolean = false,          // z.B. nach zu vielen Fehlversuchen

    // Auditing
    open var createdAt: Instant = Instant.now(),
    open var updatedAt: Instant = Instant.now()
) {
    constructor(): this(
        id           = null,
        email        = "",
        displayName  = null,
        oauthProvider = "",
        providerId   = ""
    )
}

