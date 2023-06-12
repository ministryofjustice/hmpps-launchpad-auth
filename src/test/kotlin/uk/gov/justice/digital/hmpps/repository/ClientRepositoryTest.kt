package uk.gov.justice.digital.hmpps.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.utils.DataGenerator

@SpringBootTest
@ExtendWith(SpringExtension::class)
class ClientRepositoryTest(@Autowired
                           var clientRepository: ClientRepository) {

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
    expected.description = "Update Test App"
    val result: Client = clientRepository.getById(expected.id)
    assertClient(expected, result)
  }

  @Test
  fun deleteClientById() {
    val expected: Client = DataGenerator.buildClient()
    expected.description = "Update Test App"
    val result: Client = clientRepository.getById(expected.id)
    assertClient(expected, result)
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