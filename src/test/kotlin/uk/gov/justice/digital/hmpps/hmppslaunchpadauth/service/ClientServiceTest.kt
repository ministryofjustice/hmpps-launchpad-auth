package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.BaseIntegrationTest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.REDIRECT_URI
import java.util.*

@ExtendWith(MockitoExtension::class)
class ClientServiceTest : BaseIntegrationTest() {
  @Mock
  lateinit var clientRepository: ClientRepository
  private lateinit var clientService: ClientService
  private lateinit var client: Client

  @BeforeEach
  fun setUp() {
    clientService = ClientService(clientRepository)
    client = DataGenerator.buildClient(true, true)
  }

  @AfterEach
  fun tearDown() {
    clientRepository.deleteAll()
  }

  @Test
  fun `validate params`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    clientService.validateParams(
      client.id,
      "code",
      Scope.USER_BASIC_READ.toString(),
      "https://launchpad.com",
      UUID.randomUUID().toString(),
      "",
    )
  }

  @Test
  fun `validate params with null state and null nonce`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    clientService.validateParams(
      client.id,
      "code",
      Scope.USER_BASIC_READ.toString(),
      REDIRECT_URI,
      null,
      null,
    )
  }

  @Test
  fun `validate params when client record do not exist`() {
    val clientId = UUID.randomUUID()
    Mockito.`when`(clientRepository.findById(clientId)).thenReturn(Optional.empty())
    val exception = assertThrows(ApiException::class.java) {
      clientService.validateParams(
        clientId,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        Scope.USER_BASIC_READ.toString(),
        "jhuyt",
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(HttpStatus.FORBIDDEN, exception.code)
  }

  @Test
  fun `validate param invalid url value`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    val exception = assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        "code",
        Scope.USER_BASIC_READ.toString(),
        "jhuyt",
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(HttpStatus.FORBIDDEN, exception.code)
  }

  @Test
  fun `validate param enabled is false`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(DataGenerator.buildClient(false, true)))
    val exception = assertThrows(SsoException::class.java) {
      clientService.validateParams(
        client.id,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        Scope.USER_BASIC_READ.toString(),
        REDIRECT_URI,
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(HttpStatus.FOUND, exception.code)
  }

  @Test
  fun `validate params when invalid scope`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    val exception = assertThrows(SsoException::class.java) {
      clientService.validateParams(
        client.id,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        "random.read.scope.test random.ead.scope.randomtest",
        REDIRECT_URI,
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(HttpStatus.FOUND, exception.code)
  }

  @Test
  fun `validate params when invalid grant`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    val exception = assertThrows(SsoException::class.java) {
      clientService.validateParams(
        client.id,
        "random_code",
        "user.basic.read user.establishment.read",
        REDIRECT_URI,
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(HttpStatus.FOUND, exception.code)
  }

  @Test
  fun `validate Params when redirect url not in registered url list`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    val exception = assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        "code",
        "user.basic.read user.establishment.read",
        "$REDIRECT_URI/random",
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(HttpStatus.FORBIDDEN, exception.code)
  }
}
