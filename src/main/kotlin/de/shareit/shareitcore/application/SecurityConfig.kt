package de.shareit.shareitcore.application

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/", "/css/**", "/js/**", "/login**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/oauth2/authorization/github")        // z.B. Standard-Login-Link
                    .defaultSuccessUrl("/items", true)
            }
            .logout { logout ->
                logout
                    .logoutSuccessUrl("/")
            }
        return http.build()
    }
}
