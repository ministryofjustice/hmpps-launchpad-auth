package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource


import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config.TestConfig
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig::class)
class UserApprovedClientIntegrationTest(
  @Autowired private var userApprovedClientRepository: UserApprovedClientRepository,
  @Autowired private var clientRepository: ClientRepository,
) {
  @LocalServerPort
  private val port = 0

  private val baseUrl = "http://localhost"

  private val restTemplate: RestTemplate = RestTemplate()

  private val id = UUID.randomUUID()
  private val clientId = UUID.randomUUID()
  private val userID = "test@moj.com"
  private val localDateTime = LocalDateTime.now() // Default time zone set for config is Europe/Paris
  private val dateTimeInUTC = LocalDateTime.now(ZoneOffset.UTC)
  private lateinit var clientDBOne: Client
  private lateinit var userApprovedClientOne: UserApprovedClient
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

  @BeforeEach
  fun beforeAll() {
    clientDBOne = Client(
      clientId,
      UUID.randomUUID().toString(),
      setOf(
        Scope.USER_CLIENTS_READ,
        Scope.USER_BASIC_READ,
        Scope.USER_BOOKING_READ,
        Scope.USER_ESTABLISHMENT_READ,
        Scope.USER_CLIENTS_DELETE
      ),
      setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
      setOf("https://testdomain.com"),
      true,
      true,
      "Test App",
      "http://localhost:${port}/test",
      "This is test App",
    )
    clientRepository.save(clientDBOne)
    userApprovedClientOne = UserApprovedClient(
      id,
      userID,
      clientId,
      setOf(Scope.USER_CLIENTS_READ),
      dateTimeInUTC,
      dateTimeInUTC,
    )
    userApprovedClientRepository.save(userApprovedClientOne)
  }

  @AfterEach
  fun tearOff(){
    clientRepository.deleteAll()
    userApprovedClientRepository.deleteAll()
  }

  @Test
  fun `get user approved clients by user id`() {
    val url = URI("$baseUrl:$port/v1/users/$userID/clients?page=1&size=20")
    val response  = restTemplate.exchange(RequestEntity<Any>(HttpMethod.GET,url), object: ParameterizedTypeReference<PagedResult<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Client>> () {})
    val pagedResult = response?.body as PagedResult<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Client>
    val clients = pagedResult?.content as List<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Client>
    val clientOne  = clients[0]
    assertEquals(pagedResult?.page, 1)
    assertEquals(1, localDateTime.compareTo(dateTimeInUTC))
    assertEquals(dateTimeInUTC.format(dateTimeFormatter), clientOne?.createdDate?.format(dateTimeFormatter))
    assertEquals(clientDBOne.name, clientOne?.name)
    assertEquals(clientDBOne.id, clientOne?.id)
    assertEquals(clientDBOne.logoUri, clientOne?.logoUri)
    assertEquals(clientDBOne.description, clientOne?.description)
    assertEquals(clientDBOne.autoApprove, clientOne?.autoApprove)
  }

  @Test
  fun `revoke client access`() {
    val url = URI("$baseUrl:$port/v1/users/$userID/clients/$clientId")
    val response = restTemplate.exchange(RequestEntity<Any>(HttpMethod.DELETE,url), object: ParameterizedTypeReference<ResponseEntity<Void>> () {})
    assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    assertTrue(userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(userID, clientId).isEmpty)
  }
}