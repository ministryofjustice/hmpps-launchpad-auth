package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class BaseIntegrationTest {

  companion object {
    private val postgreSQLContainer: PostgreSQLContainer<*> =
      PostgreSQLContainer(DockerImageName.parse("postgres:17"))
        .withDatabaseName("launchpad-test")
        .withUsername("launchpad")
        .withPassword("launchpad")
        .withReuse(true)
        .apply { start() }

    @JvmStatic
    @DynamicPropertySource
    fun datasourceConfig(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { postgreSQLContainer.jdbcUrl }
      registry.add("spring.datasource.username") { postgreSQLContainer.username }
      registry.add("spring.datasource.password") { postgreSQLContainer.password }
    }
  }
}
