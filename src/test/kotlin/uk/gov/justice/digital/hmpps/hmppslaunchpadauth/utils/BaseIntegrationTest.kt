package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest internal constructor() {

  @Autowired
  private lateinit var flyway: Flyway

  companion object {

    private val postgreSQLContainer = PostgresContainer.repositoryContainer

    @JvmStatic
    @DynamicPropertySource
    fun datasourceConfig(registry: DynamicPropertyRegistry) {
      postgreSQLContainer?.run {
        registry.add("spring.datasource.url") { postgreSQLContainer.jdbcUrl }
        registry.add("spring.datasource.username") { postgreSQLContainer.username }
        registry.add("spring.datasource.password") { postgreSQLContainer.password }
        registry.add("spring.flyway.clean-disabled") { "false" }
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl)
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername)
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword)
      }
    }
  }

  @BeforeAll
  internal fun beforeAll() {
    flyway.clean()
    flyway.migrate()
  }
}
