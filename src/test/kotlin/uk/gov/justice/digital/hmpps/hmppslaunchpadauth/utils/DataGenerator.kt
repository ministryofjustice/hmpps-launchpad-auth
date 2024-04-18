package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.User
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.AccessTokenPayload
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenCommonClaims
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenGenerationAndValidation
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

const val REDIRECT_URI = "https://launchpad.com"
const val LOGO_URI = "$REDIRECT_URI/logo"
const val USER_ID = "G2320VD"

class DataGenerator {
  val CLIENT_ID = UUID.randomUUID()

  companion object {
    fun buildClient(enabled: Boolean, autoApprove: Boolean): Client {
      return Client(
        UUID.randomUUID(),
        UUID.randomUUID().toString(),
        setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ),
        setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
        setOf(REDIRECT_URI),
        enabled,
        autoApprove,
        "Test App",
        LOGO_URI,
        "Test App for test environment",
      )
    }

    fun buildSsoRequest(): SsoRequest {
      return SsoRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        LocalDateTime.now(ZoneOffset.UTC),
        UUID.randomUUID(),
        SsoClient(
          UUID.randomUUID(),
          UUID.randomUUID().toString(),
          UUID.randomUUID().toString(),
          setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ),
          "http://localhost:8080/test",
        ),
        "testuser@test.com",
      )
    }

    fun jwtBuilder(issue: Instant, exp: Instant, nonce: UUID, userId: String?, secret: String): String {
      val privateKey = getPrivateKey(secret)
      val issueDate = Date.from(issue)
      val expDate = Date.from(exp)
      return Jwts.builder()
        .setIssuer("RandomIssuer")
        .setSubject("login")
        .setAudience("test audience")
        // .claim("preferred_username", "testuser@test.com")
        .claim("name", "Test User")
        .claim("scope", "openid")
        .claim("nonce", nonce)
        .claim("email", userId)
        .claim("tid", "123456_random_value")
        .setIssuedAt(issueDate)
        .setExpiration(expDate)
        .signWith(
          privateKey,
          SignatureAlgorithm.RS256,
        )
        .compact()
    }

    fun buildUserApprovedClient(
      userId: String,
      clientId: UUID,
      scopes: Set<Scope>,
      createdDate: LocalDateTime,
      lastModifiedDate: LocalDateTime,
    ): UserApprovedClient {
      return UserApprovedClient(
        UUID.randomUUID(),
        userId,
        clientId,
        scopes,
        createdDate,
        lastModifiedDate,
      )
    }

    /*fun createValidAccessToken() {
      val password = UUID.randomUUID().toString()
      val scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
      val grants = setOf(AuthorizationGrantType.AUTHORIZATION_CODE)
      val redirectUri = REDIRECT_URI
      val logoUri = LOGO_URI
      val clientId = UUID.randomUUID()
      val randomSecret = UUID.randomUUID().toString()
      val client = Client(
        clientId,
        BCryptPasswordEncoder().encode(password),
        scopes,
        grants,
        setOf(redirectUri),
        true,
        true,
        "Test App",
        logoUri,
        "Test Description",
      )
      val userApprovedScopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
      val userApprovedClient = UserApprovedClient(
        UUID.randomUUID(),
        USER_ID,
        clientId,
        userApprovedScopes,
        LocalDateTime.now(ZoneOffset.UTC),
        LocalDateTime.now(ZoneOffset.UTC),
      )
      val accessTokenPayload = AccessTokenPayload()
      // val nonce = "random_nonce"
      val payload = accessTokenPayload.generatePayload(
        User(USER_ID, "John",  "Smith"),
        clientId,
        userApprovedScopes,
      )
      val authHeader = "Bearer " + TokenGenerationAndValidation.generateToken(
        payload,
        TokenCommonClaims.buildHeaderClaims(),
        randomSecret,
      )
    }*/

    fun generateAccessToken(
      client: Client,
      userApprovedClient: UserApprovedClient,
      nonce: String?,
      secret: String,
      kid: String,
      validityInSeconds: Long,
    ): String {
      val accessTokenPayload = AccessTokenPayload()
      val nonce = "random_nonce"
      val payload = accessTokenPayload.generatePayload(
        User(USER_ID, "John", "Smith"),
        client.id,
        userApprovedClient.scopes,
        validityInSeconds,
      )
      return "Bearer " + TokenGenerationAndValidation.generateJwtToken(
        payload,
        TokenCommonClaims.buildHeaderClaims(kid),
        secret,
      )
    }

    fun getPrivateKey(secret: String): PrivateKey {
      try {
        val privateKeyFormatted = secret
          .trimIndent()
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replace("-----END PRIVATE KEY-----", "")
          .replace("\\s".toRegex(), "")
        val privateKeyInBytes = Base64.getDecoder().decode(privateKeyFormatted)
        return KeyFactory.getInstance("RSA").generatePrivate(
          PKCS8EncodedKeySpec(privateKeyInBytes),
        )
      } catch (e: Exception) {
        val message = "Error converting private key string to private key object ${e.message}"
        throw ApiException(
          message,
          HttpStatus.INTERNAL_SERVER_ERROR,
          ApiErrorTypes.SERVER_ERROR.toString(),
          ApiErrorTypes.SERVER_ERROR.toString(),
        )
      }
    }

    fun generateRandomRSAKey(): KeyPair {
      val rsaGenerator = KeyPairGenerator.getInstance("RSA")
      rsaGenerator.initialize(4096)
      return rsaGenerator.genKeyPair()
    }

    fun getPublicKey(secret: String): PublicKey {
      try {
        val publicKeyFormatted = secret
          .trimIndent()
          .replace("-----BEGIN PUBLIC KEY-----", "")
          .replace("-----END PUBLIC KEY-----", "")
          .replace("\\s".toRegex(), "")
        val publicKeyInBytes = Base64.getDecoder().decode(publicKeyFormatted)
        return KeyFactory.getInstance("RSA").generatePublic(
          X509EncodedKeySpec(publicKeyInBytes),
        )
      } catch (e: Exception) {
        val message = "Error converting public key string to public key object ${e.message}"
        throw ApiException(
          message,
          HttpStatus.INTERNAL_SERVER_ERROR,
          ApiErrorTypes.SERVER_ERROR.toString(),
          ApiErrorTypes.SERVER_ERROR.toString(),
        )
      }
    }
  }
}
