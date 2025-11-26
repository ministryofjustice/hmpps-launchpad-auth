package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

object PostgresContainer {
  val repositoryContainer: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }

  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing>? = PostgreSQLContainer<Nothing>("postgres:17").apply {
    withEnv("HOSTNAME_EXTERNAL", "localhost")
    withDatabaseName("launchpad-test")
    withUsername("launchpad")
    withPassword("launchpad")
    setWaitStrategy(Wait.forListeningPort())
    withReuse(true)
    start()
  }
}
