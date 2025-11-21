package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.net.ServerSocket

object PostgresContainer {
  val repositoryContainer: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }

  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing>? {
    if (isPostgresRunning()) {
      //return null
    }
    return PostgreSQLContainer<Nothing>("postgres:17").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withDatabaseName("launchpad-test")
      withUsername("launchpad")
      withPassword("launchpad")
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)
      start()
    }
  }

  private fun isPostgresRunning(): Boolean = try {
    val serverSocket = ServerSocket(5432)
    serverSocket.localPort == 0
  } catch (error: IOException) {
    true
  }
}
