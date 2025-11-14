package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest internal constructor() {

  companion object {

    private val postgreSQLContainer = PostgresContainer.repositoryContainer

    @JvmStatic
    @DynamicPropertySource
    fun datasourceConfig(registry: DynamicPropertyRegistry) {
      postgreSQLContainer?.run {
        registry.add("spring.datasource.url") { postgreSQLContainer.jdbcUrl }
        registry.add("spring.datasource.username") { postgreSQLContainer.username }
        registry.add("spring.datasource.password") { postgreSQLContainer.password }
      }
    }
  }
}
