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
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.LOGO_URI
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.REDIRECT_URI
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@SpringBootTest(classes = [SsoLogInService::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class SsoLogInServiceTest(@Autowired private var ssoLoginService: SsoLogInService) {
  @MockBean
  private lateinit var ssoRequestService: SsoRequestService

  @MockBean
  private lateinit var clientService: ClientService

  @MockBean
  private lateinit var tokenProcessor: TokenProcessor

  @MockBean
  private lateinit var userApprovedClientService: UserApprovedClientService

  @Value("\${launchpad.auth.private-key}")
  private lateinit var secret: String

  private lateinit var ssoRequest: SsoRequest
  private lateinit var client: Client
  private val userID = "G2320VD"

  @BeforeEach
  fun setUp() {
    client = Client(
      UUID.randomUUID(),
      UUID.randomUUID().toString(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ),
      setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
      setOf(REDIRECT_URI),
      true,
      true,
      "Test App",
      LOGO_URI,
      "Test App for test environment",
    )
    ssoRequest = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      LocalDateTime.now(ZoneOffset.UTC),
      UUID.randomUUID(),
      SsoClient(
        client.id,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ),
        REDIRECT_URI,
      ),
      userID,
    )
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `test initiate sign in`() {
    Mockito.`when`(
      ssoRequestService.generateSsoRequest(
        setOf(Scope.USER_BASIC_READ),
        ssoRequest.id.toString(),
        ssoRequest.client.nonce,
        ssoRequest.client.redirectUri,
        ssoRequest.client.id,
      ),
    ).thenReturn(ssoRequest)
    Mockito.`when`(
      clientService.validateParams(
        ssoRequest.client.id,
        "code",
        Scope.USER_BASIC_READ.toString(),
        ssoRequest.client.redirectUri,
        ssoRequest.id.toString(),
        ssoRequest.client.nonce,
      ),
    ).thenReturn(client)
    val url = ssoLoginService.initiateSsoLogin(
      ssoRequest.client.id,
      "code",
      Scope.USER_BASIC_READ.toString(),
      ssoRequest.client.redirectUri,
      ssoRequest.id.toString(),
      ssoRequest.client.nonce,
    )
    val result = URL(url.toString())
    assertNotNull(result)
  }

  @Test
  fun `test update sso request with user id when auto approved is true`() {
    val nonce = UUID.randomUUID()
    val token = DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, userID, secret)
    ssoRequest.userId = null
    Mockito.`when`(ssoRequestService.getSsoRequestById(ssoRequest.id)).thenReturn(Optional.of(ssoRequest))
    Mockito.`when`(ssoRequestService.updateSsoRequest(any())).thenReturn(ssoRequest)
    Mockito.`when`(tokenProcessor.getUserId(token, nonce.toString())).thenReturn(userID)
    Mockito.`when`(clientService.getClientById(ssoRequest.client.id)).thenReturn(Optional.of(client))
    val redirectView = ssoLoginService.updateSsoRequest(
      token,
      ssoRequest.id,
    ) as RedirectView
    assertNotNull(redirectView)
  }

  @Test
  fun `test update sso request with user id when auto approved is false`() {
    val nonce = UUID.randomUUID()
    val token = DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, userID, secret)
    ssoRequest.userId = null
    Mockito.`when`(ssoRequestService.getSsoRequestById(ssoRequest.id)).thenReturn(Optional.of(ssoRequest))
    Mockito.`when`(ssoRequestService.updateSsoRequest(any())).thenReturn(ssoRequest)
    Mockito.`when`(tokenProcessor.getUserId(token, nonce.toString())).thenReturn(userID)
    Mockito.`when`(clientService.getClientById(ssoRequest.client.id)).thenReturn(Optional.of(client))
    val redirectView = ssoLoginService.updateSsoRequest(
      token,
      ssoRequest.id,
    ) as RedirectView
    assertNotNull(redirectView)
  }

  @Test
  fun `test update sso request with user id after user approved`() {
    Mockito.`when`(ssoRequestService.getSsoRequestById(ssoRequest.id)).thenReturn(Optional.of(ssoRequest))
    Mockito.`when`(ssoRequestService.updateSsoRequest(any())).thenReturn(ssoRequest)
    Mockito.`when`(clientService.getClientById(ssoRequest.client.id)).thenReturn(Optional.of(client))
    val redirectView = ssoLoginService.updateSsoRequest(
      null,
      ssoRequest.id,
    ) as RedirectView
    assertNotNull(redirectView)
  }

  @Test
  fun `test update sso request with user id when sso request not found`() {
    Mockito.`when`(ssoRequestService.getSsoRequestById(ssoRequest.id)).thenReturn(Optional.empty())
    val exception = assertThrows(ApiException::class.java) {
      ssoLoginService.updateSsoRequest(
        null,
        ssoRequest.id,
      )
    }
    assertEquals(HttpStatus.FORBIDDEN, exception.code)
  }
}
