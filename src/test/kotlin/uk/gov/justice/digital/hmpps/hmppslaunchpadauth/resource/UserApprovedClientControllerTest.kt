package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.Authentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationUserInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.USER_ID
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.util.*

@SpringBootTest(classes = [UserApprovedClientController::class, UserIdValidator::class])
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
    Mockito.`when`(userApprovedClientService.getUserApprovedClientsByUserId(userId, 1, size = 10)).thenReturn(
      PagedResult(1, true, 1, listOf()),
    )
  }
}
