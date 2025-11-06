package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import io.zonky.test.db.AutoConfigureEmbeddedDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.jdbc.JdbcTestUtils
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import java.util.*

@DataJpaTest
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository"])
@EntityScan("uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model")
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class ClientRepositoryTest(@Autowired var clientRepository: ClientRepository) {
  @BeforeEach
  fun setUp(@Autowired jdbcTemplate: JdbcTemplate) {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "client")
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `create client`() {
    val expected: Client = DataGenerator.buildClient(true, true)
    val result = clientRepository.save(expected)
    assertClient(expected, result)
  }

  @Test
  fun `update client`() {
    var expected: Client = DataGenerator.buildClient(false, false)
    expected = Client(
      expected.id,
      expected.secret,
      expected.scopes,
      expected.authorizedGrantTypes,
      expected.registeredRedirectUris,
      expected.enabled,
      expected.autoApprove,
      expected.name,
      expected.logoUri,
      "Update Test App",
    )
    val result = clientRepository.save(expected)
    assertClient(expected, result)
  }

  @Test
  fun `get client by id`() {
    val expected: Client = DataGenerator.buildClient(false, false)
    clientRepository.save(expected)
    val result: Optional<Client> = clientRepository.findById(expected.id)
    assertClient(expected, result.get())
  }

  @Test
  fun `delete client by id`() {
    val expected: Client = DataGenerator.buildClient(false, false)
    clientRepository.save(expected)
    clientRepository.deleteById(expected.id)
  }

  private fun assertClient(result: Client, expected: Client) {
    assertEquals(expected.id, result.id)
    assertEquals(expected.autoApprove, result.autoApprove)
    assertEquals(expected.enabled, result.enabled)
    assertEquals(expected.logoUri, result.logoUri)
    assertEquals(expected.name, result.name)
    assertEquals(expected.authorizedGrantTypes, result.authorizedGrantTypes)
    assertEquals(expected.scopes, result.scopes)
    assertEquals(expected.description, result.description)
  }
}
