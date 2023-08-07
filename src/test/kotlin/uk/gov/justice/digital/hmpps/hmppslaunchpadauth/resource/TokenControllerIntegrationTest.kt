package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.LOGO_URI
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.REDIRECT_URI
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.USER_ID
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TokenControllerIntegrationTest(
  @Autowired private var userApprovedClientRepository: UserApprovedClientRepository,
  @Autowired private var clientRepository: ClientRepository,
  @Autowired private var ssoRequestRepository: SsoRequestRepository,
  @Autowired private var encoder: BCryptPasswordEncoder,
) {
  @LocalServerPort
  private val port = 0

  private val baseUrl = "http://localhost"

  private val restTemplate: RestTemplate = RestTemplate()

  private val id = UUID.randomUUID()
  private val clientId = UUID.randomUUID()
  private val userID = "G2320VD"
  private val localDateTime = LocalDateTime.now() // Default time zone set for config is Europe/Paris
  private val dateTimeInUTC = LocalDateTime.now(ZoneOffset.UTC)
  private lateinit var clientDBOne: Client
  private lateinit var userApprovedClientOne: UserApprovedClient
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
  private lateinit var authorizationHeader: String
  private lateinit var ssoRequest: SsoRequest
  private val clientState: String = "12345"
  private val clientSecret = UUID.randomUUID()
  private val clientNonce: String = "client_nonce"
  private val code = UUID.randomUUID()

  @BeforeEach
  fun beforeEach() {
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
        setOf(Scope.USER_CLIENTS_READ, Scope.USER_CLIENTS_DELETE),
        REDIRECT_URI,
      ),
      USER_ID,
    )
    ssoRequestRepository.save(ssoRequest)
    userApprovedClientOne = UserApprovedClient(
      id,
      userID,
      clientId,
      setOf(Scope.USER_CLIENTS_READ, Scope.USER_CLIENTS_DELETE),
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
    ssoRequestRepository.deleteAll()
  }

  @Test
  fun `get token`() {
    val headers = LinkedMultiValueMap<String, String>();
    headers.add("Authorization", authorizationHeader)
    var url = URI("$baseUrl:$port/v1/token?code=$code&grant_type=code&redirect_uri=$REDIRECT_URI")
    var response = restTemplate.exchange(
      RequestEntity<Any>(headers, HttpMethod.GET, url),
      object : ParameterizedTypeReference<Token>() {},
    )
    var token = response.body
    assertNotNull(token?.idToken)
    assertNotNull(token?.accessToken)
    assertNotNull(token?.refreshToken)
    assertEquals("Bearer", token?.tokenType)
    assertEquals(3600L, token?.expiresIn)
    url = URI("$baseUrl:$port/v1/token?nonce=anything&refresh_token=${response.body.refreshToken}")
    response = restTemplate.exchange(
      RequestEntity<Any>(headers, HttpMethod.GET, url),
      object : ParameterizedTypeReference<Token>() {},
    )
    token = response.body
    assertNotNull(token?.idToken)
    assertNotNull(token?.accessToken)
    assertNotNull(token?.refreshToken)
    assertEquals("Bearer", token?.tokenType)
    assertEquals(3600L, token?.expiresIn)
  }
}
