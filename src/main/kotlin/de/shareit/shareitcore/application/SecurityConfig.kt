package de.shareit.shareitcore.application


import de.shareit.shareitcore.application.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
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
                    "/h2-console/**",
                    "/categories/**"
                ).permitAll()
//                    .requestMatchers(HttpMethod.POST, "/categories").hasRole("ADMIN")
//                    .requestMatchers(HttpMethod.GET, "/categories", "/categories/new").hasRole("ADMIN")
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
