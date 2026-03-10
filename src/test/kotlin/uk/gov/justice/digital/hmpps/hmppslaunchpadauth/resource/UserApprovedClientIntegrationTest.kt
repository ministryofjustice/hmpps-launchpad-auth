package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserApprovedClientDto
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.BaseIntegrationTest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.LOGO_URI
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.REDIRECT_URI
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserApprovedClientIntegrationTest(
  @Autowired private var userApprovedClientRepository: UserApprovedClientRepository,
  @Autowired private var clientRepository: ClientRepository,
  @Autowired private var webClientBuilder: WebClient.Builder,
  @Value("\${launchpad.auth.access-token-validity-seconds}")
  private var accessTokenValiditySeconds: Long,
) : BaseIntegrationTest() {
  @LocalServerPort
  private val port = 0

  private val baseUrl = "http://localhost"

  private val id = UUID.randomUUID()
  private val clientId = UUID.randomUUID()
  private val userID = "G2320VD"
  private val localDateTime = LocalDateTime.now() // Default time zone set for config is Europe/Paris
  private val dateTimeInUTC = LocalDateTime.now(ZoneOffset.UTC)
  private lateinit var clientDBOne: Client
  private lateinit var userApprovedClientOne: UserApprovedClient
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
  private lateinit var authorizationHeader: String

  @Value("\${launchpad.auth.private-key}")
  private lateinit var privateKey: String

  @Value("\${launchpad.auth.kid}")
  private lateinit var kid: String

  @Value("\${launchpad.auth.iss-url}")
  private lateinit var issUrl: String

  @BeforeEach
  fun beforeEach() {
    clientRepository.deleteAll()
    userApprovedClientRepository.deleteAll()
    clientDBOne = Client(
      clientId,
      UUID.randomUUID().toString(),
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
    userApprovedClientOne = UserApprovedClient(
      id,
      userID,
      clientId,
      setOf(Scope.USER_CLIENTS_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTC,
      dateTimeInUTC,
    )
    userApprovedClientRepository.save(userApprovedClientOne)
    authorizationHeader = DataGenerator.generateAccessToken(
      clientDBOne,
      userApprovedClientOne,
      "test nonce",
      privateKey,
      kid,
      issUrl,
      accessTokenValiditySeconds,
    )
  }

  @AfterEach
  fun tearOff() {
    clientRepository.deleteAll()
    userApprovedClientRepository.deleteAll()
  }

  @Test
  fun `get user approved clients by user id`() {
    var webClient = webClientBuilder
      .baseUrl("$baseUrl:$port")
      .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeader)
      .build()

    val response = webClient.get()
      .uri("/v1/users/$userID/clients?page=1&size=20")
      .retrieve()
      .toEntity(object : ParameterizedTypeReference<PagedResult<UserApprovedClientDto>>() {})
      .block()

    val pagedResult = (response?.body ?: null) as PagedResult<UserApprovedClientDto>
    val userApprovedClientDtos = pagedResult.content
    val clientOne = userApprovedClientDtos[0]
    val scopes: List<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope> = clientOne.scopes
    scopes.forEach { scope ->
      assertFalse(scope.type == Scope.USER_BOOKING_READ.toString())
    }
    assertEquals(pagedResult.totalElements, 1)
    assertEquals(1, localDateTime.compareTo(dateTimeInUTC))
    assertEquals(dateTimeInUTC.format(dateTimeFormatter), clientOne.createdDate.format(dateTimeFormatter))
    assertEquals(clientDBOne.name, clientOne.name)
    assertEquals(clientDBOne.id, clientOne.id)
    assertEquals(clientDBOne.logoUri, clientOne.logoUri)
    assertEquals(clientDBOne.description, clientOne.description)
    assertEquals(clientDBOne.autoApprove, clientOne.autoApprove)
  }

  @Test
  fun `revoke client access`() {
    var webClient = webClientBuilder
      .baseUrl("$baseUrl:$port")
      .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeader)
      .build()

    val response = webClient.delete()
      .uri("/v1/users/$userID/clients/$clientId")
      .retrieve()
      .toEntity(object : ParameterizedTypeReference<ResponseEntity<Void>>() {})
      .block()
    assertEquals(HttpStatus.NO_CONTENT, response?.statusCode)
    assertTrue(userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(userID, clientId).isEmpty)
  }
}
