package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonEstablisments

@Configuration

class AuthServiceConfig {

  @Bean
  fun bCryptPasswordEncoder() : BCryptPasswordEncoder {
    return BCryptPasswordEncoder()
  }

  @Bean("establishments")
  fun establishments(): PrisonEstablisments {
    return PrisonEstablisments()
  }
}
