package de.shareit.shareitcore.application

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

// 3) Security-Config: CustomOAuth2UserService einbinden
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
open class SecurityConfig(
    private val userService: UserService
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF bleibt aktiv (nur H2-Console ausgenommen)
            .csrf { csrf ->
                csrf.ignoringRequestMatchers("/h2-console/**")
            }
            .headers { headers ->
                headers.frameOptions { frame ->
                    frame.sameOrigin()
                }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/items").permitAll()
                    // ► LISTEN: "/items" (z. B. Item-Übersicht) darf öffentlich bleiben
                    .requestMatchers("/items").permitAll()

                    // ► FORMULARE: "/items/new" und "/items/{id}/edit" nur für eingeloggte Nutzer
                    .requestMatchers(HttpMethod.GET, "/items/new", "/items/*/edit").authenticated()
                    .requestMatchers(HttpMethod.POST, "/items/new", "/items/*/edit").authenticated()

                    // ► SONSTIGES: z. B. Kategorien-Endpoints offenlassen
                    .requestMatchers(HttpMethod.GET,"/categories/new").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST,"/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET,"/categories").permitAll()

                    // ► STATISCHE RESSOURCEN
                    .requestMatchers(
                        "/", "/css/**", "/js/**", "/error",
                        "/oauth2/**", "/login/**", "/h2-console/**"
                    ).permitAll()

                    // Alles andere (z. B. /items/{id}, /items/{id}/delete) erfordert Auth
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .userInfoEndpoint { endpoints ->
                        endpoints.userService(userService)
                    }
                    .defaultSuccessUrl("/items", true)
            }
            .logout {
                it.logoutSuccessUrl("/")
            }
        return http.build()
    }
}
