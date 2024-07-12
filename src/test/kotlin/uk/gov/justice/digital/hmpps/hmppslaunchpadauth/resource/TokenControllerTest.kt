package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationUserInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.BasicAuthentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import java.net.URI
import java.time.Instant
import java.util.*

@SpringBootTest(classes = [TokenController::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class TokenControllerTest {

  @MockBean
  private lateinit var tokenService: TokenService

  private lateinit var tokenController: TokenController
  private val redirectUri = ""
  private val code = UUID.randomUUID()
  private val clientId = UUID.randomUUID()
  private val nonce = UUID.randomUUID()

  @MockBean
  @Qualifier("basicAuthentication")
  private lateinit var basicAuthentication: BasicAuthentication

  @Value("\${launchpad.auth.private-key}")
  private lateinit var privateKey: String

  private lateinit var authenticationInfo: AuthenticationInfo
  private lateinit var token: String
  private val authheader = "Basic" + Base64.getEncoder().encodeToString("random string".toByteArray())

  @BeforeEach
  fun setUp() {
    authenticationInfo = AuthenticationUserInfo(
      UUID.randomUUID(),
      UUID.randomUUID().toString(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ),
    )
    tokenController = TokenController(tokenService, basicAuthentication)
    token = DataGenerator.jwtBuilder(
      Instant.now(),
      Instant.now().plusSeconds(3600),
      nonce,
      null,
      privateKey,
      "123456_random_value",
    )
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun generateTokenByCode() {
    Mockito.`when`(
      tokenService.validateRequestAndGenerateToken(
        code,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        URI(redirectUri),
        clientId,
        null,
        authenticationInfo,
        null,
      ),
    ).thenReturn(Token(token, token, token, "Bearer", 3600L))
    Mockito.`when`(basicAuthentication.authenticate(authheader)).thenReturn(authenticationInfo)
    val response = tokenController.generateToken(
      code,
      AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
      URI(redirectUri),
      null,
      any(),
      authheader,
    )
    assertEquals(HttpStatus.OK, response.statusCode)
  }

  @Test
  fun generateTokenByRefreshToken() {
    Mockito.`when`(
      tokenService.validateRequestAndGenerateToken(
        code,
        AuthorizationGrantType.REFRESH_TOKEN.toString(),
        URI(redirectUri),
        clientId,
        token,
        authenticationInfo,
        null,
      ),
    ).thenReturn(Token(token, token, token, "Bearer", 3600L))
    Mockito.`when`(basicAuthentication.authenticate(authheader)).thenReturn(authenticationInfo)
    val response = tokenController.generateToken(
      null,
      AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
      URI(redirectUri),
      token,
      any(),
      authheader,
    )
    assertEquals(HttpStatus.OK, response.statusCode)
  }
}
