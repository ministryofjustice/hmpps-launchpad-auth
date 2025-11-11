package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.BaseIntegrationTest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import java.util.*

@ExtendWith(MockitoExtension::class)
class SsoRequestServiceTest : BaseIntegrationTest() {
  @Mock
  private lateinit var ssoRequestRepository: SsoRequestRepository
  private lateinit var ssoRequestService: SsoRequestService
  private lateinit var ssoRequest: SsoRequest

  @BeforeEach
  fun setUp() {
    ssoRequestService = SsoRequestService(ssoRequestRepository)
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
    val result = ssoRequestService.updateSsoRequest(ssoRequest)
    assertSsoRequest(ssoRequest, result)
  }

  @Test
  fun getSsoRequestById() {
    val id = UUID.randomUUID()
    Mockito.`when`(ssoRequestRepository.findById(id)).thenReturn(Optional.of(ssoRequest))
    val result = ssoRequestService.getSsoRequestById(id)
    assertSsoRequest(ssoRequest, result.get())
  }

  @Test
  fun deleteSsoRequestById() {
    val id = UUID.randomUUID()
    Mockito.doNothing().`when`(ssoRequestRepository).deleteById(id)
    ssoRequestService.deleteSsoRequestById(id)
  }

  @Test
  fun generateSsoRequest() {
    Mockito.`when`(ssoRequestRepository.save(any())).thenReturn(ssoRequest)
    val result = ssoRequestService.generateSsoRequest(
      ssoRequest.client.scopes,
      ssoRequest.client.state,
      ssoRequest.client.nonce,
      ssoRequest.client.redirectUri,
      ssoRequest.client.id,
    )
    assertSsoRequest(ssoRequest, result)
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
    assertEquals(expected.redirectUri, result.redirectUri)
    assertEquals(expected.nonce, result.nonce)
  }
}
