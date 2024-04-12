package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
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
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminEndpointIntegrationTest(
  @Autowired private var userApprovedClientRepository: UserApprovedClientRepository,
  @Autowired private var clientRepository: ClientRepository,
  @Autowired private var ssoRequestRepository: SsoRequestRepository,
  @Autowired private var encoder: BCryptPasswordEncoder,
) {
  @LocalServerPort
  private val port = 0

  // @Value("\${launchpad.auth.secret}")
  // private lateinit var secret: String

  private val baseUrl = "http://localhost"

  private val restTemplate: RestTemplate = RestTemplate()

  private val id = UUID.randomUUID()
  private val clientIdOne = UUID.randomUUID()
  private val clientIdSecond = UUID.randomUUID()
  private val userIDFirst = "userIDFirst"
  private val userIDSecond = "userIDSecond"
  private val userIDThird = "userIDThird"
  private val userIDFourth = "userIDFourth"
  private val userIDFifth = "userIDFifth"
  private val userIDSixth = "userIDSixth"
  private val userIDSeventh = "userIDSeventh"
  private val dateTimeInUTCBefore7Years = LocalDateTime.now(ZoneOffset.UTC).minusYears(7).minusMinutes(1)
  private val dateTimeInUTCBefore3Years = LocalDateTime.now(ZoneOffset.UTC).minusYears(3)
  private val dateTimeInUTCBefore5Minutes = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(11)
  private val dateTimeInUTCBefore2Minutes = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(2)
  private lateinit var clientDBOne: Client
  private lateinit var clientDBSecond: Client
  private lateinit var userApprovedClientOne: UserApprovedClient
  private lateinit var userApprovedClientSecond: UserApprovedClient
  private lateinit var userApprovedClientThird: UserApprovedClient
  private lateinit var userApprovedClientFourth: UserApprovedClient
  private lateinit var userApprovedClientFifth: UserApprovedClient
  private lateinit var userApprovedClientSixth: UserApprovedClient
  private lateinit var userApprovedClientSeventh: UserApprovedClient
  private lateinit var userApprovedClientEight: UserApprovedClient
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
  private lateinit var authorizationHeader: String
  private lateinit var ssoRequestFirst: SsoRequest
  private lateinit var ssoRequestSecond: SsoRequest
  private lateinit var ssoRequestThird: SsoRequest
  private lateinit var ssoRequestFourth: SsoRequest
  private lateinit var ssoRequestFifth: SsoRequest
  private lateinit var ssoRequestSixth: SsoRequest
  private lateinit var ssoRequestSeventh: SsoRequest
  private val clientState: String = "12345"
  private val clientSecret = UUID.randomUUID()
  private val clientNonce: String = "client_nonce"

  @BeforeEach
  fun beforeEach() {
    clientRepository.deleteAll()
    ssoRequestRepository.deleteAll()
    userApprovedClientRepository.deleteAll()

    clientDBOne = Client(
      clientIdOne,
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
    clientDBSecond = Client(
      clientIdOne,
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
    clientDBSecond = Client(
      clientIdOne,
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
    clientRepository.saveAll(Arrays.asList(clientDBOne, clientDBSecond))

    ssoRequestFirst = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      dateTimeInUTCBefore5Minutes,
      UUID.randomUUID(),
      SsoClient(
        clientIdOne,
        clientState,
        clientNonce,
        setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
        REDIRECT_URI,
      ),
      userIDFirst,
    )
    ssoRequestSecond = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      dateTimeInUTCBefore5Minutes,
      UUID.randomUUID(),
      SsoClient(
        clientIdOne,
        clientState,
        clientNonce,
        setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
        REDIRECT_URI,
      ),
      userIDSecond,
    )
    ssoRequestThird = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      dateTimeInUTCBefore5Minutes,
      UUID.randomUUID(),
      SsoClient(
        clientIdOne,
        clientState,
        clientNonce,
        setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
        REDIRECT_URI,
      ),
      userIDThird,
    )
    ssoRequestFourth = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      dateTimeInUTCBefore5Minutes,
      UUID.randomUUID(),
      SsoClient(
        clientIdOne,
        clientState,
        clientNonce,
        setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
        REDIRECT_URI,
      ),
      userIDFourth,
    )
    ssoRequestFifth = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      dateTimeInUTCBefore5Minutes,
      UUID.randomUUID(),
      SsoClient(
        clientIdOne,
        clientState,
        clientNonce,
        setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
        REDIRECT_URI,
      ),
      userIDFifth,
    )
    ssoRequestSixth = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      dateTimeInUTCBefore5Minutes,
      UUID.randomUUID(),
      SsoClient(
        clientIdSecond,
        clientState,
        clientNonce,
        setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
        REDIRECT_URI,
      ),
      userIDSixth,
    )
    ssoRequestSeventh = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      dateTimeInUTCBefore2Minutes,
      UUID.randomUUID(),
      SsoClient(
        clientIdSecond,
        clientState,
        clientNonce,
        setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
        REDIRECT_URI,
      ),
      userIDSeventh,
    )
    ssoRequestRepository.saveAll(
      Arrays.asList(
        ssoRequestFirst,
        ssoRequestSecond,
        ssoRequestThird,
        ssoRequestFourth,
        ssoRequestFifth,
        ssoRequestSixth,
        ssoRequestSeventh,
      ),
    )

    userApprovedClientOne = UserApprovedClient(
      UUID.randomUUID(),
      userIDFirst,
      clientIdOne,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore7Years,
    )
    userApprovedClientSecond = UserApprovedClient(
      UUID.randomUUID(),
      userIDSecond,
      clientIdOne,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore7Years,
    )
    userApprovedClientThird = UserApprovedClient(
      UUID.randomUUID(),
      userIDThird,
      clientIdOne,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore7Years,
    )
    userApprovedClientFourth = UserApprovedClient(
      UUID.randomUUID(),
      userIDFourth,
      clientIdOne,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore7Years,
    )
    userApprovedClientFifth = UserApprovedClient(
      UUID.randomUUID(),
      userIDFifth,
      clientIdOne,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore7Years,
    )
    userApprovedClientSixth = UserApprovedClient(
      UUID.randomUUID(),
      userIDSixth,
      clientIdOne,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore7Years,
    )
    userApprovedClientSeventh = UserApprovedClient(
      UUID.randomUUID(),
      userIDSeventh,
      clientIdSecond,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore3Years,
    )
    userApprovedClientEight = UserApprovedClient(
      UUID.randomUUID(),
      userIDSixth,
      clientIdSecond,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore3Years,
    )
    userApprovedClientRepository.saveAll(
      Arrays.asList(
        userApprovedClientOne,
        userApprovedClientSecond,
        userApprovedClientThird,
        userApprovedClientFourth,
        userApprovedClientFifth,
        userApprovedClientSixth,
        userApprovedClientSeventh,
        userApprovedClientEight,
      ),
    )
  }

  @AfterEach
  fun tearOff() {
    clientRepository.deleteAll()
    ssoRequestRepository.deleteAll()
    userApprovedClientRepository.deleteAll()
  }

  @Test
  fun `test purge stale sso requests`() {
    Assertions.assertEquals(7, ssoRequestRepository.findAll().size)
    var url = URI("$baseUrl:$port/v1/admin/purge-stale-sso-tokens")
    var response = restTemplate.exchange(
      RequestEntity<Any>(null, HttpMethod.POST, url),
      String.javaClass,
    )
    Assertions.assertEquals(HttpStatus.ACCEPTED.value(), response.statusCode.value())
    Assertions.assertEquals(1, ssoRequestRepository.findAll().size)
  }

  @Test
  fun `test purge inactive users approved clients of inactive users`() {
    Assertions.assertEquals(8, userApprovedClientRepository.findAll().size)
    var url = URI("$baseUrl:$port/v1/admin/purge-inactive-users")
    var response = restTemplate.exchange(
      RequestEntity<Any>(null, HttpMethod.POST, url),
      String.javaClass,
    )
    Assertions.assertEquals(HttpStatus.ACCEPTED.value(), response.statusCode.value())
    Assertions.assertEquals(3, userApprovedClientRepository.findAll().size)
  }

  @Test
  fun `test purge inactive users when for a single user has one active and other inactive users approved clients`() {
    userApprovedClientRepository.deleteAll()
    val uacFirst = UserApprovedClient(
      UUID.randomUUID(),
      userIDFirst,
      clientIdOne,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore7Years,
    )
    val uacSecond = UserApprovedClient(
      UUID.randomUUID(),
      userIDFirst,
      clientIdSecond,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore3Years,
      dateTimeInUTCBefore3Years,
    )
    userApprovedClientRepository.saveAll(Arrays.asList(uacFirst, uacSecond))
    Assertions.assertEquals(2, userApprovedClientRepository.findAll().size)
    var url = URI("$baseUrl:$port/v1/admin/purge-inactive-users")
    var response = restTemplate.exchange(
      RequestEntity<Any>(null, HttpMethod.POST, url),
      String.javaClass,
    )
    Assertions.assertEquals(HttpStatus.ACCEPTED.value(), response.statusCode.value())
    Assertions.assertEquals(2, userApprovedClientRepository.findAll().size)
  }

  @Test
  fun `test purge inactive users when for a single user has all inactive users approved clients`() {
    userApprovedClientRepository.deleteAll()
    val uacFirst = UserApprovedClient(
      UUID.randomUUID(),
      userIDFirst,
      clientIdOne,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore7Years,
    )
    val uacSecond = UserApprovedClient(
      UUID.randomUUID(),
      userIDFirst,
      clientIdSecond,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_ESTABLISHMENT_READ, Scope.USER_CLIENTS_DELETE),
      dateTimeInUTCBefore7Years,
      dateTimeInUTCBefore7Years,
    )
    userApprovedClientRepository.saveAll(Arrays.asList(uacFirst, uacSecond))
    Assertions.assertEquals(2, userApprovedClientRepository.findAll().size)
    var url = URI("$baseUrl:$port/v1/admin/purge-inactive-users")
    var response = restTemplate.exchange(
      RequestEntity<Any>(null, HttpMethod.POST, url),
      String.javaClass,
    )
    Assertions.assertEquals(HttpStatus.ACCEPTED.value(), response.statusCode.value())
    Assertions.assertEquals(0, userApprovedClientRepository.findAll().size)
  }
}
