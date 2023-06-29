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
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.util.*

const val ACCESS_DENIED = "Access denied"
const val IN_VALID_SCOPE = "The requested scope is invalid or not found."
const val IN_VALID_GRANT = "The requested response type is invalid or not found."
const val IN_VALID_REDIRECT_URI = "The requested redirect uri is invalid or not found"
const val BAD_REQUEST_CODE = 400
const val ACCESS_DENIED_CODE = 403

@ExtendWith(MockitoExtension::class)
class ClientServiceTest {
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
  fun `validate params with null state and null nonce`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    clientService.validateParams(
      client.id,
      AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
      Scope.USER_BASIC_READ.toString(),
      "http://localhost:8080/test",
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
    assertEquals(ACCESS_DENIED, exception.message)
    assertEquals(ACCESS_DENIED_CODE, exception.code)
  }

  @Test
  fun `validate param invalid url value`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    val exception = assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        Scope.USER_BASIC_READ.toString(),
        "jhuyt",
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(IN_VALID_REDIRECT_URI, exception.message)
    assertEquals(BAD_REQUEST_CODE, exception.code)
  }

  @Test
  fun `validate param enabled is false`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(DataGenerator.buildClient(false, true)))
    val exception = assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        Scope.USER_BASIC_READ.toString(),
        "http://localhost:8080/test",
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(ACCESS_DENIED, exception.message)
    assertEquals(ACCESS_DENIED_CODE, exception.code)
  }

  @Test
  fun `validate params when invalid scope`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    val exception = assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        "random.read.scope.test random.ead.scope.randomtest",
        "http://localhost:8080/test",
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(IN_VALID_SCOPE, exception.message)
    assertEquals(BAD_REQUEST_CODE, exception.code)
  }

  @Test
  fun `validate params when invalid grant`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    val exception = assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        "random_code",
        "user.basic.read user.establishment.read",
        "http://localhost:8080/test",
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(IN_VALID_GRANT, exception.message)
    assertEquals(BAD_REQUEST_CODE, exception.code)
  }

  @Test
  fun `validate Params when redirect url not in registered url list`() {
    Mockito.`when`(clientRepository.findById(client.id)).thenReturn(Optional.of(client))
    val exception = assertThrows(ApiException::class.java) {
      clientService.validateParams(
        client.id,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        "user.basic.read user.establishment.read",
        "http://localhost:8080/test1",
        UUID.randomUUID().toString(),
        "test",
      )
    }
    assertEquals(IN_VALID_REDIRECT_URI, exception.message)
    assertEquals(BAD_REQUEST_CODE, exception.code)
  }
}
