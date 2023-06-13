package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.util.*

@ExtendWith(MockitoExtension::class)
class ClientServiceTest {
  @Mock
  lateinit var clientRepository: ClientRepository
  private lateinit var clientService: ClientService
  private lateinit var client: Client

  @BeforeEach
  fun setUp() {
    clientService = ClientService(clientRepository)
    client = DataGenerator.buildClient()
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `validate params`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    clientService.validateParams(
      client.id,
      AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
      Scope.USER_BASIC_READ.toString(),
      "http://localhost:8080/test",
      UUID.randomUUID().toString(),
      "",
    )
  }

  @Test
  fun `validate param invalid url value`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        Scope.USER_BASIC_READ.toString(),
        "jhuyt",
        UUID.randomUUID().toString(),
        "test",
      )
    }
  }

  @Test
  fun `validate params when invalid scope`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        "random.ead.scope.test,random.ead.scope.randomtest,",
        "http://localhost:8080/test",
        UUID.randomUUID().toString(),
        "test",
      )
    }
  }

  @Test
  fun `validate params when invalid grant`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        "random_code",
        "random.ead.scope.test,random.ead.scope.randomtest,",
        "http://localhost:8080/test",
        UUID.randomUUID().toString(),
        "test",
      )
    }
  }

  @Test
  fun `validate Params when redirect url not in registered url list`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        "random.read.scope.test",
        "http://localhost:8080/test1",
        UUID.randomUUID().toString(),
        "test",
      )
    }
  }
}
