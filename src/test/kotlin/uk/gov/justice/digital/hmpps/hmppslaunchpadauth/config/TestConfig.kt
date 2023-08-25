package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class TestConfig {

  // Default time zone is set to test LocalDateTime and conversion of time in API by Jackson and asserted in
  // integration test
  @Bean
  fun timeZone(): TimeZone? {
    val defaultTimeZone = TimeZone.getTimeZone("Europe/Paris")
    TimeZone.setDefault(defaultTimeZone)
    return defaultTimeZone
  }
}
