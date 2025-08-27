package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonEstablishments

@Configuration
class AuthServiceConfig {

  @Bean
  fun bCryptPasswordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

  @Bean
  fun webClientBuilder(): WebClient.Builder = WebClient.builder()

  @Bean("establishments")
  fun establishments(): PrisonEstablishments = PrisonEstablishments()
}
