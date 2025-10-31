package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer

@Configuration
class SecurityConfig {
  @Bean
  fun webSecurityCustomizer(): WebSecurityCustomizer? = WebSecurityCustomizer { web: WebSecurity ->
    web.ignoring().requestMatchers("/v1/**")
    // Uncomment the line below to expose public key endpoint
    // web.ignoring().requestMatchers("/.well-known/jwks.json")
  }
}
