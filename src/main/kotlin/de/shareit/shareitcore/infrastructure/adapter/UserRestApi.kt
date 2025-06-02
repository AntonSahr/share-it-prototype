package de.shareit.shareitcore.infrastructure.adapter

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping

@GetMapping("/me")

fun profile(@AuthenticationPrincipal user: OAuth2User): Map<String,Any?> =
    mapOf(
        "name"  to user.attributes["name"],
        "email" to user.attributes["email"]
    )
