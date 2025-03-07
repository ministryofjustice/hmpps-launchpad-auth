package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonEstablishments

@Configuration
class AuthServiceConfig {

  @Bean
  fun bCryptPasswordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

  @Bean("restTemplate")
  fun restTemplate(): RestTemplate = RestTemplate()

  @Bean("establishments")
  fun establishments(): PrisonEstablishments = PrisonEstablishments()
}
