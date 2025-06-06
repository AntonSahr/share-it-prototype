package de.shareit.shareitcore.application


import de.shareit.shareitcore.application.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

// 3) Security-Config: CustomOAuth2UserService einbinden
@Configuration
@EnableWebSecurity
open class SecurityConfig(
    private val userService: UserService
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { csrf ->
            csrf
                // Alle anderen Pfade CSRF-geschützt lassen, aber
                // für /h2-console/** CSRF ignorieren
                .ignoringRequestMatchers("/h2-console/**")
        }
            .headers { headers ->
                headers.frameOptions { frame ->
                    frame.sameOrigin()
                }
            }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/",
                    "/items",
                    "/css/**",
                    "/js/**",
                    "/error",
                    "/oauth2/**",
                    "/login/**",
                    "/h2-console/**"
                ).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .userInfoEndpoint { endpoints ->
                        endpoints.userService(userService)
                    }
                    .defaultSuccessUrl("/items", true)
            }
            .logout { it.logoutSuccessUrl("/") }
        return http.build()
    }
}
