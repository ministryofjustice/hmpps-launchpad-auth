package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.HmppsLaunchpadAuth
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.BaseIntegrationTest

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [HmppsLaunchpadAuth::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
abstract class IntegrationTestBase : BaseIntegrationTest() {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient
}
