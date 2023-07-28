package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import java.util.*

@SpringBootTest(classes = [UserApprovedClientController::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class UserApprovedClientControllerTest(@Autowired private var userApprovedClientController: UserApprovedClientController) {
  @MockBean
  private lateinit var userApprovedClientService: UserApprovedClientService

  private val userId = "G2320VD"

  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `get user approved clients by user id`() {
    Mockito.`when`(userApprovedClientService.getUserApprovedClientsByUserId(userId, 1, size = 10)).thenReturn(
      PagedResult(1, true, 1, listOf()),
    )
    val response = userApprovedClientController.getUserApprovedClients(userId, 1, 10)
    val pagedResult = response.body
    assertEquals(1, pagedResult?.totalElements)
    assertEquals(0, pagedResult?.content?.size)
  }

  @Test
  fun `get user approved clients by user id with page number null`() {
    Mockito.`when`(userApprovedClientService.getUserApprovedClientsByUserId(userId, 1, 10)).thenReturn(
      PagedResult(1, true, 1, listOf()),
    )
    val response = userApprovedClientController.getUserApprovedClients(userId, null, 10)
    val pagedResult = response.body
    assertEquals(1, pagedResult?.totalElements)
    assertEquals(0, pagedResult?.content?.size)
  }

  @Test
  fun `get user approved clients by user id with page size null`() {
    Mockito.`when`(userApprovedClientService.getUserApprovedClientsByUserId(userId, 1, 20)).thenReturn(
      PagedResult(1, true, 1, listOf()),
    )
    val response = userApprovedClientController.getUserApprovedClients(userId, 1, null)
    val pagedResult = response.body
    assertEquals(1, pagedResult?.totalElements)
    assertEquals(0, pagedResult?.content?.size)
  }

  @Test
  fun `get user approved clients by user id with page number less than 1`() {
    assertThrows(ApiException::class.java) { userApprovedClientController.getUserApprovedClients(userId, 0, null) }
  }

  @Test
  fun `get user approved clients by user id with when user id format is invalid`() {
    assertThrows(ApiException::class.java) {
      userApprovedClientController.getUserApprovedClients(
        "test@random.com",
        null,
        null,
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
