package uk.gov.justice.digital.hmpps.hmppslaunchpadauth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication()
@EnableConfigurationProperties
class HmppsLaunchpadAuth

fun main(args: Array<String>) {
  runApplication<HmppsLaunchpadAuth>(*args)
}
