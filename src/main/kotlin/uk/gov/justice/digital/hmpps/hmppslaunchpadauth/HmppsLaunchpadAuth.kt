package uk.gov.justice.digital.hmpps.hmppslaunchpadauth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication()
@EnableConfigurationProperties
@EnableCaching
class HmppsLaunchpadAuth

fun main(args: Array<String>) {
  runApplication<HmppsLaunchpadAuth>(*args)
}
