package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.net.URL
import java.time.Instant
import java.util.*

@SpringBootTest(classes = [SsoLoginService::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class SsoLoginServiceTest {
  @MockBean
  private lateinit var ssoRequestService: SsoRequestService
  @MockBean
  private lateinit var clientService: ClientService
  @MockBean
  private lateinit var tokenProcessor: TokenProcessor
  @Autowired
  private lateinit var ssoLoginService: SsoLoginService

  private val ssoRequest = DataGenerator.buildSsoRequest();


  @BeforeEach
    fun setUp() {
    }



    @AfterEach
    fun tearDown() {
    }

    @Test
    fun initiateSsoLogin() {
      Mockito.`when`(ssoRequestService.getSsoRequestById(ssoRequest.id)).thenReturn(Optional.of(ssoRequest))
      Mockito.doNothing().`when`(clientService).validateParams(
        ssoRequest.client.id,
        "code",
        Scope.USER_BASIC_READ.toString() + " " +Scope.USER_BOOKING_READ.toString(),
        ssoRequest.client.reDirectUri,
        ssoRequest.id.toString(),
        "",
      )
      val url = ssoLoginService.initiateSsoLogin(
        ssoRequest.client.id,
        "code",
        Scope.USER_BASIC_READ.toString() + " " + Scope.USER_BOOKING_READ.toString(),
        ssoRequest.client.reDirectUri,
        ssoRequest.id.toString(),
        "",
      )
      val result = URL(url)
      assertNotNull(result)
    }

    @Test
    fun generateAndUpdateSsoRequestWithAuthorizationCode() {
      val token = DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600))
      Mockito.`when`(ssoRequestService.updateSsoRequestAuthCodeAndUserId("vrnkmr110@outlook.com", ssoRequest.id)).thenReturn(ssoRequest.client.reDirectUri)
      Mockito.`when`(tokenProcessor.getUserId(token)).thenReturn("vrnkmr110@outlook.com")
      val url = ssoLoginService.generateAndUpdateSsoRequestWithAuthorizationCode(
        token,
        ssoRequest.id,
      )
      val result = URL(url)
      assertNotNull(result)
    }
}