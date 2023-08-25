package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
@Configuration
class AuthServiceConfig {
  @Bean
  fun bCryptPasswordEncoder() : BCryptPasswordEncoder {
    return BCryptPasswordEncoder()
  }
}
