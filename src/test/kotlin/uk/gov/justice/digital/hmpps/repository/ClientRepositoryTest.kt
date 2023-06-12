package uk.gov.justice.digital.hmpps.repository

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.util.*

@SpringBootTest(classes = [ClientService::class])
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository"])
@EntityScan("uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model")
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class ClientRepositoryTest(
  @Autowired var clientRepository: ClientRepository
  ) {
  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }
  @Test
  fun createClient() {
    val expected: Client = DataGenerator.buildClient()
    val result = clientRepository.save(expected)
    assertClient(expected, result)
  }

  @Test
  fun updateClient() {
    val expected: Client = DataGenerator.buildClient()
    expected.description = "Update Test App"
    val result = clientRepository.save(expected)
    assertClient(expected, result)
  }

  @Test
  fun getClientById() {
    val expected: Client = DataGenerator.buildClient()
    clientRepository.save(expected)
    val result: Optional<Client> = clientRepository.findById(expected.id)
    assertClient(expected, result.get())
  }

  @Test
  fun deleteClientById() {
    val expected: Client = DataGenerator.buildClient()
    clientRepository.save(expected)
    expected.description = "Update Test App"
    clientRepository.deleteById(expected.id)
  }

  fun assertClient(result: Client, expected: Client) {
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