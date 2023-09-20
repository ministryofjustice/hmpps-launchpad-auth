package uk.gov.justice.digital.hmpps.hmppslaunchpadauth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.PrisonEstablisments

@SpringBootApplication()
@EnableConfigurationProperties(PrisonEstablisments::class)
class HmppsLaunchpadAuth

fun main(args: Array<String>) {
  runApplication<HmppsLaunchpadAuth>(*args)
}
