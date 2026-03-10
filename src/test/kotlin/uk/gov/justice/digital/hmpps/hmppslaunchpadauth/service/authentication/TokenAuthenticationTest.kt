package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import com.fasterxml.uuid.Generators
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
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
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
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.BaseIntegrationTest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator.Companion.generateRandomRSAKey
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
class TokenAuthenticationTest(
  @Autowired private var tokenAuthentication: TokenAuthentication,
  @Value("\${launchpad.auth.access-token-validity-seconds}")
  private var accessTokenValiditySeconds: Long,
) : BaseIntegrationTest() {

  @MockitoBean
  private lateinit var userIdValidator: UserIdValidator

  @Value("\${launchpad.auth.private-key}")
  private lateinit var privateKey: String

  @Value("\${launchpad.auth.private-key}")
  private lateinit var publicKey: String

  @Value("\${launchpad.auth.kid}")
  private lateinit var kid: String

  @Value("\${launchpad.auth.iss-url}")
  private lateinit var issUrl: String

  private val encoder = BCryptPasswordEncoder()
  private val password = UUID.randomUUID().toString()
  private val scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
  private val grants = setOf(AuthorizationGrantType.AUTHORIZATION_CODE)
  private val redirectUri = REDIRECT_URI
  private val logoUri = LOGO_URI
  private val clientId = UUID.randomUUID()
  private val userId = USER_ID
  private val userApprovedScopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
  private val encodedPassword: String = encoder.encode(password) as String

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
      issUrl,
      accessTokenValiditySeconds,
    )
    val authHeader = "Bearer " + TokenGenerationAndValidation.generateJwtToken(
      payload,
      TokenCommonClaims.buildHeaderClaims(kid),
      privateKey,
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
    val keyPair = generateRandomRSAKey()
    val verifyingSignature = String(keyPair.public.encoded)
    publicKey = verifyingSignature
    val accessTokenPayload = AccessTokenPayload()
    val nonce = "random_nonce"
    val payload = accessTokenPayload.generatePayload(
      User(USER_ID, "John", "Smith"),
      clientId,
      userApprovedScopes,
      issUrl,
      accessTokenValiditySeconds,
    )
    val authHeader = "Bearer " + TokenGenerationAndValidation.generateJwtToken(
      payload,
      TokenCommonClaims.buildHeaderClaims(kid),
      Base64.getEncoder().encodeToString(keyPair.private.encoded),
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

  private fun buildClient(enabled: Boolean): Client = Client(
    clientId,
    encodedPassword,
    scopes,
    grants,
    setOf(redirectUri),
    enabled,
    true,
    "Test App",
    logoUri,
    "Test Description",
  )

  private fun buildUserApprovedClient(): UserApprovedClient = UserApprovedClient(
    Generators.timeBasedEpochGenerator().generate(),
    USER_ID,
    clientId,
    userApprovedScopes,
    LocalDateTime.now(ZoneOffset.UTC),
    LocalDateTime.now(ZoneOffset.UTC),
  )
}
