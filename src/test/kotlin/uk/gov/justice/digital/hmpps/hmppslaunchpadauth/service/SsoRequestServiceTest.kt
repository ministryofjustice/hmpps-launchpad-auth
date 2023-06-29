package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.util.*

@ExtendWith(MockitoExtension::class)
class SsoRequestServiceTest {
  @Mock
  private lateinit var ssoRequestRepository: SsoRequestRepository
  @Mock
  private  lateinit var clientService: ClientService
  private lateinit var ssoRequestService: SsoRequestService
  private lateinit var ssoRequest: SsoRequest
  private lateinit var client: Client

  @BeforeEach
  fun setUp() {
    ssoRequestService = SsoRequestService(ssoRequestRepository, clientService)
    ssoRequest = DataGenerator.buildSsoRequest()
  }

  @AfterEach
  fun tearDown() {
  }

    @Test
    fun createSsoRequest() {
      Mockito.`when`(ssoRequestRepository.save(ssoRequest)).thenReturn(ssoRequest)
      val result = ssoRequestService.createSsoRequest(ssoRequest)
      assertSsoRequest(ssoRequest, result)
    }

    @Test
    fun updateSsoRequest() {
      Mockito.`when`(ssoRequestRepository.save(ssoRequest)).thenReturn(ssoRequest)
      val result = ssoRequestService.createSsoRequest(ssoRequest)
      assertSsoRequest(ssoRequest, result)
    }

    @Test
    fun getSsoRequestById() {
      Mockito.`when`(ssoRequestRepository.save(ssoRequest)).thenReturn(ssoRequest)
      val result = ssoRequestService.createSsoRequest(ssoRequest)
      assertSsoRequest(ssoRequest, result)
    }

    @Test
    fun generateSsoRequest() {
      Mockito.`when`(ssoRequestRepository.save(any())).thenReturn(ssoRequest)
      val result = ssoRequestService.generateSsoRequest(
        ssoRequest.client.scopes,
        ssoRequest.client.state,
        ssoRequest.client.nonce,
        ssoRequest.client.reDirectUri,
        ssoRequest.client.id
      )
      assertEquals(ssoRequest.id, result)
    }

    @Test
    fun updateSsoRequestAuthCodeAndUserId() {
      ssoRequest.authorizationCode = null
      Mockito.`when`(ssoRequestRepository.save(ssoRequest)).thenReturn(ssoRequest)
      val client = Client(
        ssoRequest.client.id,
        UUID.randomUUID().toString(),
        setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ),
        setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
        setOf("http://localhost:8080/test"),
        true,
        true,
        "Test App",
        "http://localhost:8080/test",
        "Update Test App",
      )
      //Mockito.`when`(clientService.getClientById(ssoRequest.client.id)).thenReturn(Optional.of(client))
      Mockito.`when`(ssoRequestRepository.findById(UUID.fromString(ssoRequest.client.state))).thenReturn(Optional.of(ssoRequest))
      val result = ssoRequest.userId?.let {
        ssoRequestService.updateSsoRequestAuthCodeAndUserId(
          ssoRequest.userId.toString(),
          UUID.fromString(ssoRequest.client.state)
        )
      }
      assertEquals(true, result?.contains("http://localhost:8080/test?"))
    }

  @Test
  fun updateSsoRequestAuthCodeAndUserIdWhenAuthCodeAlreadyUsed() {
    //Mockito.`when`(ssoRequestRepository.save(ssoRequest)).thenReturn(ssoRequest)
    val client = Client(
      ssoRequest.client.id,
      UUID.randomUUID().toString(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ),
      setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
      setOf("http://localhost:8080/test"),
      true,
      true,
      "Test App",
      "http://localhost:8080/test",
      "Update Test App",
    )
    //Mockito.`when`(clientService.getClientById(ssoRequest.client.id)).thenReturn(Optional.of(client))
    Mockito.`when`(ssoRequestRepository.findById(UUID.fromString(ssoRequest.client.state))).thenReturn(Optional.of(ssoRequest))
    val exception = assertThrows(ApiException::class.java) {
      ssoRequest.userId?.let {
        ssoRequestService.updateSsoRequestAuthCodeAndUserId(
          ssoRequest.userId.toString(),
          UUID.fromString(ssoRequest.client.state)
        )
      }
    }
    assertEquals(400, exception.code)
  }

    @Test
    fun getClient() {
      Mockito.`when`(ssoRequestRepository.findById(ssoRequest.id)).thenReturn(Optional.of(ssoRequest))
      val client = Client(
        ssoRequest.client.id,
        UUID.randomUUID().toString(),
        setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ),
        setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
        setOf("http://localhost:8080/test"),
        true,
        true,
        "Test App",
        "http://localhost:8080/test",
        "Update Test App",
      )
      Mockito.`when`(clientService.getClientById(ssoRequest.client.id)).thenReturn(Optional.of(client))
      val result = ssoRequestService.getClient(ssoRequest.id)
      assertEquals(ssoRequest.client.id, result.id)
    }

  private fun assertSsoRequest(expected: SsoRequest, result: SsoRequest) {
    assertEquals(expected.id, result.id)
    assertEquals(expected.createdDate, result.createdDate)
    assertEquals(expected.authorizationCode, result.authorizationCode)
    assertEquals(expected.nonce, result.nonce)
    assertEquals(expected.userId, result.userId)
    assertSsoClient(expected.client, result.client)
  }

  private fun assertSsoClient(expected: SsoClient, result: SsoClient) {
    assertEquals(expected.id, result.id)
    assertEquals(expected.state, result.state)
    assertEquals(expected.scopes, result.scopes)
    assertEquals(expected.reDirectUri, result.reDirectUri)
    assertEquals(expected.nonce, result.nonce)
  }
}