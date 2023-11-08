package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.AccessTokenPayload
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenCommonClaims
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenGenerationAndValidation
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.LOGO_URI
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.REDIRECT_URI
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.USER_ID
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@SpringBootTest(classes = [TokenAuthentication::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class TokenAuthenticationTest(@Autowired private var tokenAuthentication: TokenAuthentication) {

  @MockBean
  private lateinit var userIdValidator: UserIdValidator

  @Value("\${launchpad-auth.secret}")
  private lateinit var secret: String

  private val encoder = BCryptPasswordEncoder()
  private val password = UUID.randomUUID().toString()
  private val scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
  private val grants = setOf(AuthorizationGrantType.AUTHORIZATION_CODE)
  private val redirectUri = REDIRECT_URI
  private val logoUri = LOGO_URI
  private val clientId = UUID.randomUUID()
  private val userId = USER_ID
  private val userApprovedScopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)

  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun authenticate() {
    val accessTokenPayload = AccessTokenPayload()
    val nonce = "random_nonce"
    val payload = accessTokenPayload.generatePayload(
      User(USER_ID, "John", "Smith"),
      clientId,
      userApprovedScopes,
    )
    val authHeader = "Bearer " + TokenGenerationAndValidation.generateJwtToken(
      payload,
      TokenCommonClaims.buildHeaderClaims(),
      secret,
    )
    Mockito.`when`(userIdValidator.isValid(userId)).thenReturn(true)
    val authenticationUserInfo = tokenAuthentication.authenticate(authHeader) as AuthenticationUserInfo
    assertEquals(authenticationUserInfo.clientId, clientId)
    assertEquals(authenticationUserInfo.userId, userId)
    assertEquals(authenticationUserInfo.userApprovedScope, userApprovedScopes)
  }

  @Test
  fun `authenticate token created from other secret`() {
    val randomSecret = "random_secret_xxx_random_secret_xxx"
    val accessTokenPayload = AccessTokenPayload()
    val nonce = "random_nonce"
    val payload = accessTokenPayload.generatePayload(
      User(USER_ID, "John", "Smith"),
      clientId,
      userApprovedScopes,
    )
    val authHeader = "Bearer " + TokenGenerationAndValidation.generateJwtToken(
      payload,
      TokenCommonClaims.buildHeaderClaims(),
      randomSecret,
    )
    val exception = assertThrows(ApiException::class.java) {
      tokenAuthentication.authenticate(authHeader)
    }
    assertEquals(HttpStatus.FORBIDDEN, exception.code)
  }

  /*@Test
  fun `authenticate when client is disabled`() {
    val client = buildClient(false)
    val accessTokenPayload = AccessTokenPayload()
    val nonce = "random_nonce"
    val payload = accessTokenPayload.generatePayload(
      User(USER_ID, "John Smith", "Smith"),
      clientId,
      userApprovedScopes,
    )
    val authHeader = "Bearer " + TokenGenerationAndValidation.generateToken(
      payload,
      TokenCommonClaims.buildHeaderClaims(),
      secret,
    )
    val exception = assertThrows(ApiException::class.java) {
      tokenAuthentication.authenticate(authHeader)
    }
    assertEquals(HttpStatus.FORBIDDEN.value(), exception.code)
  }*/

  @Test
  fun `authenticate when auth header has invalid token format`() {
    val authHeader = "Test vvghhy6y "
    val exception = assertThrows(ApiException::class.java) {
      tokenAuthentication.authenticate(authHeader)
    }
    assertEquals(HttpStatus.FORBIDDEN, exception.code)
  }

  private fun buildClient(enabled: Boolean): Client {
    return Client(
      clientId,
      encoder.encode(password),
      scopes,
      grants,
      setOf(redirectUri),
      enabled,
      true,
      "Test App",
      logoUri,
      "Test Description",
    )
  }

  private fun buildUserApprovedClient(): UserApprovedClient {
    return UserApprovedClient(
      UUID.randomUUID(),
      USER_ID,
      clientId,
      userApprovedScopes,
      LocalDateTime.now(ZoneOffset.UTC),
      LocalDateTime.now(ZoneOffset.UTC),
    )
  }
}
