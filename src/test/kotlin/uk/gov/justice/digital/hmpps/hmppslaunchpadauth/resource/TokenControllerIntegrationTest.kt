package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.claims
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.ApiError
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserApprovedClientDto
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenGenerationAndValidation
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator.Companion.getPrivateKey
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.LOGO_URI
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.REDIRECT_URI
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.USER_ID
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TokenControllerIntegrationTest(
  @Autowired private var userApprovedClientRepository: UserApprovedClientRepository,
  @Autowired private var clientRepository: ClientRepository,
  @Autowired private var ssoRequestRepository: SsoRequestRepository,
  @Autowired private var encoder: BCryptPasswordEncoder,
  @Value("\${launchpad.auth.access-token-validity-seconds}")
  private var accessTokenValiditySeconds: Long,
  @Autowired
  var webClientBuilder: WebClient.Builder,
) {
  @LocalServerPort
  private val port = 0

  @Value("\${launchpad.auth.private-key}")
  private lateinit var privateKey: String

  @Value("\${launchpad.auth.public-key}")
  private lateinit var publicKey: String

  private val baseUrl = "http://localhost"
  private val id = UUID.randomUUID()
  private val clientId = UUID.randomUUID()
  private val userID = "G2320VD"
  private val dateTimeInUTC = LocalDateTime.now(ZoneOffset.UTC)
  private lateinit var clientDBOne: Client
  private lateinit var userApprovedClientOne: UserApprovedClient
  private lateinit var authorizationHeader: String
  private lateinit var ssoRequest: SsoRequest
  private val clientState: String = "12345"
  private val clientSecret = UUID.randomUUID()
  private val clientNonce: String = "client_nonce"
  private val code = UUID.randomUUID()

  private val wiremock = WireMockServer(8085)

  @BeforeEach
  fun beforeEach() {
    wiremock.start()
    WireMock.configureFor("localhost", wiremock.port())
    WireMock.stubFor(
      WireMock.post(WireMock.urlEqualTo("/oauth/token?grant_type=client_credentials")).willReturn(
        WireMock.aResponse()
          .withStatus(HttpStatus.OK.value())
          .withHeader("Content-Type", "application/json")
          .withBody(
            "{   \"access_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}",
          ),
      ),
    )
    WireMock.stubFor(
      WireMock.get(WireMock.urlEqualTo("/api/bookings/offenderNo/G2320VD?fullInfo=false&extraInfo=false&csraSummary=false"))
        .willReturn(
          WireMock.aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader("Content-Type", "application/json")
            .withBody(
              "{\n" +
                "    \"offenderNo\": \"G2320VD\",\n" +
                "    \"bookingId\": 99999,\n" +
                "    \"bookingNo\": \"88888\",\n" +
                "    \"offenderId\": 66666,\n" +
                "    \"rootOffenderId\": 55555,\n" +
                "    \"firstName\": \"Test\",\n" +
                "    \"middleName\": \"x\",\n" +
                "    \"lastName\": \"User\",\n" +
                "    \"dateOfBirth\": \"2005-11-28\",\n" +
                "    \"activeFlag\": true,\n" +
                "    \"agencyId\": \"WLI\",\n" +
                "    \"assignedLivingUnitId\": 5555\n" +
                "}",
            ),
        ),
    )
    clientRepository.deleteAll()
    userApprovedClientRepository.deleteAll()
    clientDBOne = Client(
      clientId,
      encoder.encode(clientSecret.toString()),
      setOf(
        Scope.USER_CLIENTS_READ,
        Scope.USER_BASIC_READ,
        Scope.USER_BOOKING_READ,
        Scope.USER_ESTABLISHMENT_READ,
        Scope.USER_CLIENTS_DELETE,
      ),
      setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
      setOf(REDIRECT_URI),
      true,
      false,
      "Test App",
      LOGO_URI,
      "This is test App",
    )
    clientRepository.save(clientDBOne)
    ssoRequest = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      dateTimeInUTC,
      code,
      SsoClient(
        clientId,
        clientState,
        clientNonce,
        setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
        REDIRECT_URI,
      ),
      USER_ID,
    )
    ssoRequestRepository.save(ssoRequest)
    userApprovedClientOne = UserApprovedClient(
      id,
      userID,
      clientId,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTC,
      dateTimeInUTC,
    )
    userApprovedClientRepository.save(userApprovedClientOne)
    authorizationHeader =
      "Basic " + Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray(Charsets.UTF_8))
  }

  @AfterEach
  fun tearOff() {
    clientRepository.deleteAll()
    userApprovedClientRepository.deleteAll()
    wiremock.stop()
  }

  @Test
  fun `get token and use token for api call`() {
    // confirm sso request record exist before token request
    Assertions.assertEquals(true, ssoRequestRepository.findById(ssoRequest.id).isPresent)
    var webClient = webClientBuilder
      .baseUrl("$baseUrl:$port")
      .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeader)
      .build()
    var response = webClient.post()
      .uri("/v1/oauth2/token")
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(
        BodyInserters.fromFormData("code", code.toString())
          .with("grant_type", "authorization_code")
          .with("redirect_uri", REDIRECT_URI),
      )
      .retrieve()
      .toEntity(Token::class.java)
      .block()

    var token: Token = response.body
    Assertions.assertNotNull(token.idToken)
    Assertions.assertNotNull(token.accessToken)
    Assertions.assertNotNull(token.refreshToken)
    Assertions.assertEquals("Bearer", token.tokenType)
    Assertions.assertEquals(accessTokenValiditySeconds - 1, token.expiresIn)
    // confirm ssorequest deleted
    Assertions.assertEquals(true, ssoRequestRepository.findById(ssoRequest.id).isEmpty)

    response = webClient.post()
      .uri("/v1/oauth2/token")
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(
        BodyInserters.fromFormData("refresh_token", token.refreshToken)
          .with("grant_type", "refresh_token")
          .with("nonce", "anything"),
      )
      .retrieve()
      .toEntity(Token::class.java)
      .block()

    token = response.body
    assertResponseHeaders(response.headers)
    Assertions.assertNotNull(token.idToken)
    Assertions.assertNotNull(token.accessToken)
    Assertions.assertNotNull(token.refreshToken)
    assertIdTokenClaims(token.idToken)
    assertAccessTokenClaims(token.accessToken)
    assertRefreshTokenClaims(token.refreshToken)
    Assertions.assertEquals("Bearer", token.tokenType)
    Assertions.assertEquals(accessTokenValiditySeconds - 1, token.expiresIn)

    // use expire refreshToken
    var exception = Assertions.assertThrows(WebClientResponseException::class.java) {
      val refreshToken =
        updateTokenExpireTime(token.refreshToken, Instant.now().minusSeconds(60).epochSecond)

      webClient.post()
        .uri("/v1/oauth2/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(
          BodyInserters.fromFormData("refresh_token", refreshToken.toString())
            .with("grant_type", "refresh_token")
            .with("nonce", "anything"),
        )
        .retrieve()
        .toEntity(Token::class.java)
        .doOnError { ex ->
          if (ex is org.springframework.web.reactive.function.client.WebClientResponseException) {
            throw WebClientResponseException(
              ex.statusCode.value(),
              ex.statusText,
              ex.headers,
              ex.responseBodyAsByteArray,
              null,
            )
          } else {
            throw WebClientResponseException(HttpStatus.BAD_REQUEST.value(), ex.message, null, null, null, null)
          }
        }
        .block()
    }
    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), exception.statusCode.value())
    assertResponseHeaders(exception.headers)

    // using access token in auth header
    webClient = webClientBuilder
      .baseUrl("$baseUrl:$port")
      .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .build()

    val apiResponse = webClient.get()
      .uri("/v1/users/$userID/clients?page=1&size=20")
      .retrieve()
      .toEntity(object : ParameterizedTypeReference<PagedResult<UserApprovedClientDto>>() {})
      .block()!!

    val pagedResult = apiResponse.body as PagedResult<UserApprovedClientDto>
    assertResponseHeaders(apiResponse.headers)
    Assertions.assertEquals(HttpStatus.OK.value(), apiResponse.statusCode.value())
    Assertions.assertNotNull(pagedResult.content)

    // Using id token in auth header expected response should be Http 401
    exception = Assertions.assertThrows(WebClientResponseException::class.java) {
      webClient = webClientBuilder
        .baseUrl("$baseUrl:$port")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.idToken)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

      webClient.get()
        .uri("/v1/users/$userID/clients?page=1&size=20")
        .retrieve()
        .toEntity(ApiError::class.java)
        .doOnError { ex ->
          if (ex is org.springframework.web.reactive.function.client.WebClientResponseException) {
            throw WebClientResponseException(
              ex.statusCode.value(),
              ex.statusText,
              ex.headers,
              ex.responseBodyAsByteArray,
              null,
            )
          } else {
            throw WebClientResponseException(HttpStatus.BAD_REQUEST.value(), ex.message, null, null, null, null)
          }
        }
        .block()
    }
    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), exception.statusCode.value())
    assertResponseHeaders(exception.headers)

    // Using refresh token in auth header expected response should be Http 401
    exception = Assertions.assertThrows(WebClientResponseException::class.java) {
      webClient = webClientBuilder
        .baseUrl("$baseUrl:$port")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.refreshToken)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

      webClient.get()
        .uri("/v1/users/$userID/clients?page=1&size=20")
        .retrieve()
        .toEntity(ApiError::class.java)
        .doOnError { ex ->
          if (ex is org.springframework.web.reactive.function.client.WebClientResponseException) {
            throw WebClientResponseException(
              ex.statusCode.value(),
              ex.statusText,
              ex.headers,
              ex.responseBodyAsByteArray,
              null,
            )
          } else {
            throw WebClientResponseException(HttpStatus.BAD_REQUEST.value(), ex.message, null, null, null, null)
          }
        }
        .block()
    }
    Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), exception.statusCode.value())
    assertResponseHeaders(exception.headers)
  }

  private fun assertIdTokenClaims(idToken: String) {
    Assertions.assertTrue(TokenGenerationAndValidation.validateJwtTokenSignature(idToken, publicKey))
    val claims = TokenGenerationAndValidation.parseClaims(idToken, publicKey).body
    val exp = claims["exp"] as Long
    Assertions.assertTrue(exp > Instant.now().epochSecond)
    val signInUser = getsignedInUserFromClaims(claims)
    Assertions.assertEquals(clientId.toString(), signInUser)
    Assertions.assertEquals(userID, claims["sub"])
    Assertions.assertEquals("Test User", claims["name"])
    Assertions.assertEquals("Test", claims["given_name"])
    Assertions.assertEquals("User", claims["family_name"])
  }

  private fun assertAccessTokenClaims(accessToken: String) {
    Assertions.assertTrue(TokenGenerationAndValidation.validateJwtTokenSignature(accessToken, publicKey))
    val claims = TokenGenerationAndValidation.parseClaims(accessToken, publicKey).body
    val exp = claims["exp"] as Long
    Assertions.assertTrue(exp > Instant.now().epochSecond)
    val signInUser = getsignedInUserFromClaims(claims)
    Assertions.assertEquals(clientId.toString(), signInUser)
    Assertions.assertEquals(userID, claims["sub"])
    val scopes = claims["scopes"] as ArrayList<String>
    assertScopes(scopes, userApprovedClientOne.scopes)
  }

  private fun assertRefreshTokenClaims(refreshToken: String) {
    Assertions.assertTrue(TokenGenerationAndValidation.validateJwtTokenSignature(refreshToken, publicKey))
    val claims = TokenGenerationAndValidation.parseClaims(refreshToken, publicKey).payload
    val exp = claims["exp"] as Long
    Assertions.assertTrue(exp > Instant.now().epochSecond)
    val signInUser = getsignedInUserFromClaims(claims)
    Assertions.assertEquals(clientId.toString(), signInUser)
    Assertions.assertEquals(userID, claims["sub"])
    val scopes = claims["scopes"] as ArrayList<String>
    assertScopes(scopes, userApprovedClientOne.scopes)
  }

  private fun assertScopes(scopes: ArrayList<String>, scopesEnum: Set<Scope>) {
    scopesEnum.forEach { s ->
      Assertions.assertTrue(scopes.contains(s.toString()))
    }
    Assertions.assertEquals(scopes.size, scopesEnum.size)
  }

  private fun updateTokenExpireTime(token: String, exp: Long): String {
    val jws = TokenGenerationAndValidation.parseClaims(token, publicKey)
    val claims = HashMap<String, Any>(jws.payload)
    claims["exp"] = exp
    return Jwts.builder()
      .claims().add(claims).and()
      .header().add(jws.header).and()
      .signWith(getPrivateKey(privateKey), Jwts.SIG.RS256)
      .compact()
  }

  private fun assertResponseHeaders(httpHeaders: HttpHeaders) {
    Assertions.assertEquals(listOf(MediaType.APPLICATION_JSON_VALUE), httpHeaders[HttpHeaders.CONTENT_TYPE])
    Assertions.assertEquals(
      listOf("no-cache, no-store, max-age=0, must-revalidate"),
      httpHeaders[HttpHeaders.CACHE_CONTROL],
    )
    Assertions.assertEquals(listOf("no-cache"), httpHeaders[HttpHeaders.PRAGMA])
    Assertions.assertEquals(listOf("0"), httpHeaders[HttpHeaders.EXPIRES])
    Assertions.assertEquals(listOf("DENY"), httpHeaders["X-Frame-Options"])
    Assertions.assertEquals(listOf("nosniff"), httpHeaders["X-Content-Type-Options"])
  }

  private fun getsignedInUserFromClaims(claims: Claims): String {
    val client = claims["aud"] as LinkedHashSet<Any>
    return client.first as String
  }
}
