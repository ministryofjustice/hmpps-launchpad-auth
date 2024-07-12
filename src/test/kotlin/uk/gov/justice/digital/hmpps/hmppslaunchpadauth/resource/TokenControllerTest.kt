package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.apache.tomcat.util.http.HeaderUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.BasicAuthentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import java.net.URI
import java.net.http.HttpHeaders
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
  private lateinit var authenticationInfo: AuthenticationInfo
  private lateinit var basicAuthentication: BasicAuthentication
  private lateinit var token: Token
  private val authheader = "Basic" + Base64.getEncoder().encodeToString("random string".toByteArray())

    @BeforeEach
    fun setUp() {
      tokenController = TokenController(tokenService, basicAuthentication)
     /* tokn = DataGenerator.jwtBuilder(
        Instant.now(),
        Instant.now(),
        nonce,
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString()
      )*/
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun generateTokenByCode() {
     /* Mockito.`when`(tokenService.validateRequestAndGenerateToken(
        code,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        URI(redirectUri),
        clientId,
        null,
        authenticationInfo,
        null
        )).thenReturn(DataGenerator.)*/
      val response = tokenController.generateToken(
        code,
        AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
        URI(redirectUri),
        null,
        UUID.randomUUID().toString(),
        authheader)
    }

  @Test
  fun generateTokenByRefreshToken() {
    val response = tokenController.generateToken(
      null,
      AuthorizationGrantType.AUTHORIZATION_CODE.toString(),
      URI(redirectUri),
      null,
      UUID.randomUUID().toString(),
      authheader)
  }
}