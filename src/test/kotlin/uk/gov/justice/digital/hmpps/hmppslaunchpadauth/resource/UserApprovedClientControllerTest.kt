package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config.HmppsLaunchpadAuthExceptionHandler
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.Authentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationUserInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.USER_ID
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.util.*

@SpringBootTest(classes = [UserApprovedClientController::class, UserIdValidator::class, HmppsLaunchpadAuthExceptionHandler::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class UserApprovedClientControllerTest(@Autowired private var userApprovedClientController: UserApprovedClientController) {
  @MockBean
  private lateinit var userApprovedClientService: UserApprovedClientService

  @MockBean
  @Qualifier("tokenAuthentication")
  private lateinit var tokenAuthentication: Authentication

  private val userId = "G2320VD"

  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `get user approved clients by user id`() {
    Mockito.`when`(tokenAuthentication.authenticate("Bearer x.y.z")).thenReturn(
      AuthenticationUserInfo(
        UUID.randomUUID(),
        USER_ID,
        setOf(Scope.USER_CLIENTS_READ),
      ),
    )
    Mockito.`when`(userApprovedClientService.getUserApprovedClientsByUserId(userId, 1, size = 10)).thenReturn(
      PagedResult(1, true, 1, listOf()),
    )
    val response = userApprovedClientController.getUserApprovedClients(userId, 1, 10, "Bearer x.y.z")
    val pagedResult = response.body
    assertEquals(1, pagedResult?.totalElements)
    assertEquals(0, pagedResult?.content?.size)
  }

  @Test
  fun `get user approved clients when user id has invalid format`() {
    Mockito.`when`(tokenAuthentication.authenticate("Bearer x.y.z")).thenReturn(
      AuthenticationUserInfo(
        UUID.randomUUID(),
        USER_ID,
        setOf(Scope.USER_CLIENTS_READ),
      ),
    )
    val exception = assertThrows(ApiException::class.java) {
      userApprovedClientController.getUserApprovedClients("xx yy zz", 1, 10, "Bearer x.y.z")
    }
    assertEquals(HttpStatus.BAD_REQUEST, exception.code)
  }

  @Test
  fun `get user approved clients by user id with page number null`() {
    Mockito.`when`(tokenAuthentication.authenticate("Bearer x.y.z")).thenReturn(
      AuthenticationUserInfo(
        UUID.randomUUID(),
        USER_ID,
        setOf(Scope.USER_CLIENTS_READ),
      ),
    )
    Mockito.`when`(userApprovedClientService.getUserApprovedClientsByUserId(userId, 1, 10)).thenReturn(
      PagedResult(1, true, 1, listOf()),
    )
    val response = userApprovedClientController.getUserApprovedClients(userId, null, 10, "Bearer x.y.z")
    val pagedResult = response.body
    assertEquals(1, pagedResult?.totalElements)
    assertEquals(0, pagedResult?.content?.size)
  }

  @Test
  fun `get user approved clients by user id with page size null`() {
    Mockito.`when`(tokenAuthentication.authenticate("Bearer x.y.z")).thenReturn(
      AuthenticationUserInfo(
        UUID.randomUUID(),
        USER_ID,
        setOf(Scope.USER_CLIENTS_READ),
      ),
    )
    Mockito.`when`(userApprovedClientService.getUserApprovedClientsByUserId(userId, 1, 20)).thenReturn(
      PagedResult(1, true, 1, listOf()),
    )
    val response = userApprovedClientController.getUserApprovedClients(userId, 1, null, "Bearer x.y.z")
    val pagedResult = response.body
    assertEquals(1, pagedResult?.totalElements)
    assertEquals(0, pagedResult?.content?.size)
  }

  @Test
  fun `get user approved clients by user id with page number less than 1`() {
    Mockito.`when`(tokenAuthentication.authenticate("Bearer x.y.z")).thenReturn(
      AuthenticationUserInfo(
        UUID.randomUUID(),
        USER_ID,
        setOf(Scope.USER_CLIENTS_READ),
      ),
    )
    assertThrows(ApiException::class.java) {
      userApprovedClientController.getUserApprovedClients(
        userId,
        0,
        null,
        "Bearer x.y.z",
      )
    }
  }

  @Test
  fun `get user approved clients by user id with when user id format is invalid`() {
    Mockito.`when`(tokenAuthentication.authenticate("Bearer x.y.z")).thenReturn(
      AuthenticationUserInfo(
        UUID.randomUUID(),
        "$USER_ID@gmail.com",
        setOf(Scope.USER_CLIENTS_READ),
      ),
    )
    assertThrows(ApiException::class.java) {
      userApprovedClientController.getUserApprovedClients(
        "test@random.com",
        null,
        null,
        "Bearer x.y.z",
      )
    }
  }

  @Test
  fun `revoke client access by user id and client id`() {
    val clientId = UUID.randomUUID()
    Mockito.`when`(tokenAuthentication.authenticate("Bearer x.y.z")).thenReturn(
      AuthenticationUserInfo(
        clientId,
        USER_ID,
        setOf(Scope.USER_CLIENTS_DELETE),
      ),
    )
    Mockito.doNothing().`when`(userApprovedClientService).revokeClientAccess(userId, clientId)
    val response = userApprovedClientController.revokeClientAccess(userId, clientId, "Bearer x.y.z")
    assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
  }

  @Test
  fun `revoke client access by user id and client id when client id do not match in token`() {
    val clientId = UUID.randomUUID()
    Mockito.`when`(tokenAuthentication.authenticate("Bearer x.y.z")).thenReturn(
      AuthenticationUserInfo(
        UUID.randomUUID(),
        USER_ID,
        setOf(Scope.USER_CLIENTS_DELETE),
      ),
    )
    val exception = assertThrows(ApiException::class.java) {
      userApprovedClientController.revokeClientAccess(userId, clientId, "Bearer x.y.z")
    }
    assertEquals(HttpStatus.BAD_REQUEST, exception.code)
  }
}
