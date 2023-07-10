package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.net.URL
import java.time.Instant
import java.util.*

@SpringBootTest(classes = [SsoLoginService::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class SsoLoginServiceTest(@Autowired private var ssoLoginService: SsoLoginService) {
  @MockBean
  private lateinit var ssoRequestService: SsoRequestService

  @MockBean
  private lateinit var clientService: ClientService

  @MockBean
  private lateinit var tokenProcessor: TokenProcessor

  private lateinit var ssoRequest: SsoRequest

  @BeforeEach
  fun setUp() {
    ssoRequest = DataGenerator.buildSsoRequest()
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun initiateSsoLogin() {
    Mockito.`when`(
      ssoRequestService.generateSsoRequest(
        setOf(Scope.USER_BASIC_READ),
        ssoRequest.id.toString(),
        ssoRequest.client.nonce,
        ssoRequest.client.redirectUri,
        ssoRequest.client.id,
      ),
    ).thenReturn(ssoRequest)
    Mockito.doNothing().`when`(clientService).validateParams(
      ssoRequest.client.id,
      "code",
      Scope.USER_BASIC_READ.toString(),
      ssoRequest.client.redirectUri,
      ssoRequest.id.toString(),
      ssoRequest.client.nonce,
    )
    val url = ssoLoginService.initiateSsoLogin(
      ssoRequest.client.id,
      "code",
      Scope.USER_BASIC_READ.toString(),
      ssoRequest.client.redirectUri,
      ssoRequest.id.toString(),
      ssoRequest.client.nonce,
    )
    val result = URL(url)
    assertNotNull(result)
  }

  @Test
  fun generateAndUpdateSsoRequestWithAuthorizationCodeWhenAutoApproved() {
    val nonce = UUID.randomUUID()
    val id = UUID.randomUUID()
    val token = DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, id)
    ssoRequest.userId = null
    // ssoRequest.authorizationCode = null
    Mockito.`when`(ssoRequestService.getSsoRequestById(ssoRequest.id)).thenReturn(Optional.of(ssoRequest))
    Mockito.`when`(ssoRequestService.updateSsoRequest(any())).thenReturn(ssoRequest)
    Mockito.`when`(tokenProcessor.getUserId(token, nonce.toString())).thenReturn("testuser@test.com")
    val url = ssoLoginService.generateAndUpdateSsoRequestWithUserId(
      token,
      ssoRequest.id,
      true,
    )
    val result = URL(url)
    assertNotNull(result)
  }

  @Test
  fun generateAndUpdateSsoRequestWithAuthorizationCodeWhenNotAutoApproved() {
    val nonce = UUID.randomUUID()
    val id = UUID.randomUUID()
    val token = DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, id)
    ssoRequest.userId = null
    // ssoRequest.authorizationCode = null
    Mockito.`when`(ssoRequestService.getSsoRequestById(ssoRequest.id)).thenReturn(Optional.of(ssoRequest))
    Mockito.`when`(ssoRequestService.updateSsoRequest(any())).thenReturn(ssoRequest)
    Mockito.`when`(tokenProcessor.getUserId(token, nonce.toString())).thenReturn("testuser@test.com")
    val url = ssoLoginService.generateAndUpdateSsoRequestWithUserId(
      token,
      ssoRequest.id,
      false,
    )
    val result = URL(url)
    assertNotNull(result)
  }

  @Test
  fun generateAndUpdateSsoRequestWithAuthorizationCodeWhenAfterUserApproved() {
    // ssoRequest.authorizationCode = null
    Mockito.`when`(ssoRequestService.getSsoRequestById(ssoRequest.id)).thenReturn(Optional.of(ssoRequest))
    Mockito.`when`(ssoRequestService.updateSsoRequest(any())).thenReturn(ssoRequest)
    val url = ssoLoginService.generateAndUpdateSsoRequestWithUserId(
      null,
      ssoRequest.id,
      false,
    )
    val result = URL(url)
    assertNotNull(result)
  }

  @Test
  fun generateAndUpdateSsoRequestWithAuthorizationCodeWhenSsoRequestRecordNotFound() {
    Mockito.`when`(ssoRequestService.getSsoRequestById(ssoRequest.id)).thenReturn(Optional.empty())
    val exception = assertThrows(ApiException::class.java) {
      ssoLoginService.generateAndUpdateSsoRequestWithUserId(
        null,
        ssoRequest.id,
        false,
      )
    }
    assertEquals(ACCESS_DENIED, exception.message)
    assertEquals(ACCESS_DENIED_CODE, exception.code)
  }
}
