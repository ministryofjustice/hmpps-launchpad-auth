package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.LOGO_URI
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.REDIRECT_URI
import java.util.*

@ExtendWith(MockitoExtension::class)
class BasicAuthenticationTest {
  @Mock
  private lateinit var clientService: ClientService

  private lateinit var encoder: BCryptPasswordEncoder
  private lateinit var basicAuthentication: BasicAuthentication

  @BeforeEach
  fun setUp() {
    encoder = BCryptPasswordEncoder()
    basicAuthentication = BasicAuthentication(clientService, encoder)
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `authenticate valid username and password`() {
    val password = UUID.randomUUID().toString()
    val scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
    val grants = setOf(AuthorizationGrantType.AUTHORIZATION_CODE)
    val redirectUri = REDIRECT_URI
    val logoUri = LOGO_URI
    val id = UUID.randomUUID()
    val client = Client(
      id,
      encoder.encode(password),
      scopes,
      grants,
      setOf(redirectUri),
      true,
      true,
      "Test App",
      logoUri,
      "Test Description",
    )
    Mockito.`when`(clientService.getClientById(id)).thenReturn(Optional.of(client))

    val authHeader = "Basic " + Base64.getEncoder().encodeToString("$id:$password".toByteArray(Charsets.UTF_8))
    val authenticationInfo = basicAuthentication.authenticate(authHeader)
    assertEquals(authenticationInfo.clientId, id)
    assertEquals(authenticationInfo.clientScope, scopes)
  }

  @Test
  fun `authenticate invalid username and password`() {
    val password = UUID.randomUUID().toString()
    val scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
    val grants = setOf(AuthorizationGrantType.AUTHORIZATION_CODE)
    val redirectUri = REDIRECT_URI
    val logoUri = LOGO_URI
    val id = UUID.randomUUID()
    val client = Client(
      id,
      encoder.encode(password),
      scopes,
      grants,
      setOf(redirectUri),
      true,
      true,
      "Test App",
      logoUri,
      "Test Description",
    )
    Mockito.`when`(clientService.getClientById(id)).thenReturn(Optional.of(client))

    val authHeader = "Basic " + Base64.getEncoder().encodeToString("$id:randompassword".toByteArray(Charsets.UTF_8))
    val exception = assertThrows(ApiException::class.java) {
      basicAuthentication.authenticate(authHeader)
    }
    assertEquals(exception.message, UNAUTHORIZED)
    assertEquals(exception.code, UNAUTHORIZED_CODE)
  }

  @Test
  fun `authenticate when client do not exist`() {
    val password = UUID.randomUUID().toString()
    val scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
    val grants = setOf(AuthorizationGrantType.AUTHORIZATION_CODE)
    val redirectUri = REDIRECT_URI
    val logoUri = LOGO_URI
    val id = UUID.randomUUID()
    val client = Client(
      id,
      encoder.encode(password),
      scopes,
      grants,
      setOf(redirectUri),
      true,
      true,
      "Test App",
      logoUri,
      "Test Description",
    )
    Mockito.`when`(clientService.getClientById(id)).thenReturn(Optional.empty())

    val authHeader = "Basic " + Base64.getEncoder().encodeToString("$id:$password".toByteArray(Charsets.UTF_8))
    val exception = assertThrows(ApiException::class.java) {
      basicAuthentication.authenticate(authHeader)
    }
    assertEquals(exception.message, UNAUTHORIZED)
    assertEquals(exception.code, UNAUTHORIZED_CODE)
  }

  @Test
  fun `authenticate when client is not enabled`() {
    val password = UUID.randomUUID().toString()
    val scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ)
    val grants = setOf(AuthorizationGrantType.AUTHORIZATION_CODE)
    val redirectUri = REDIRECT_URI
    val logoUri = LOGO_URI
    val id = UUID.randomUUID()
    val client = Client(
      id,
      encoder.encode(password),
      scopes,
      grants,
      setOf(redirectUri),
      false,
      true,
      "Test App",
      logoUri,
      "Test Description",
    )
    Mockito.`when`(clientService.getClientById(id)).thenReturn(Optional.of(client))

    val authHeader = "Basic " + Base64.getEncoder().encodeToString("$id:$password".toByteArray(Charsets.UTF_8))
    val exception = assertThrows(ApiException::class.java) {
      basicAuthentication.authenticate(authHeader)
    }
    assertEquals(exception.message, UNAUTHORIZED)
    assertEquals(exception.code, UNAUTHORIZED_CODE)
  }

  @Test
  fun `authenticate when auth header do not contain Basic`() {
    val authHeader = Base64.getEncoder().encodeToString("xxx:yyy".toByteArray(Charsets.UTF_8))
    val exception = assertThrows(ApiException::class.java) {
      basicAuthentication.authenticate(authHeader)
    }
    assertEquals(exception.message, UNAUTHORIZED)
    assertEquals(exception.code, UNAUTHORIZED_CODE)
  }
}
