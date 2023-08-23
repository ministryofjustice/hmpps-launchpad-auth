package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoLogInService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@SpringBootTest(classes = [AuthController::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class AuthControllerTest(@Autowired private var authController: AuthController) {

  @MockBean
  private lateinit var ssoLoginService: SsoLogInService

  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `authorize happy path`() {
    val clientId = UUID.randomUUID()
    Mockito.`when`(
      ssoLoginService.initiateSsoLogin(
        clientId,
        "code",
        "user.basic.read",
        "http://randomsite/test",
        null,
        null,
      ),
    ).thenReturn("http://localhost:8080/test")
    val redirectView = authController.authorize(
      clientId,
      "code",
      "user.basic.read",
      "http://randomsite/test",
      null,
      null,
    )
    assertNotNull(redirectView)
    assertNotNull(redirectView.url)
  }

  @Test
  fun `authorize response type is not code`() {
    val exception = assertThrows(ApiException::class.java) {
      authController.authorize(
        UUID.randomUUID(),
        "anything",
        "user.basic.read",
        "http://randomsite/test",
        "random_state",
        "random_nonce",
      )
    }
    assertEquals(302, exception.code)
    assertEquals("Response type: anything is not supported", exception.message)
  }

  @Test
  fun `authorize state length exceeds 128`() {
    val exception = assertThrows(ApiException::class.java) {
      authController.authorize(
        UUID.randomUUID(),
        "code",
        "user.basic.read",
        "http://randomsite/test",
        UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID()
          .toString(),
        "random_nonce",
      )
    }
    assertEquals(302, exception.code)
    assertEquals("state size exceeds 128 char size limit", exception.message)
  }

  @Test
  fun `authorize nonce length exceeds 128`() {
    val exception = assertThrows(ApiException::class.java) {
      authController.authorize(
        UUID.randomUUID(),
        "code",
        "user.basic.read",
        "http://randomsite/test",
        "random_state",
        UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID()
          .toString(),
      )
    }
    assertEquals(302, exception.code)
    assertEquals("nonce size exceeds 128 char size limit", exception.message)
  }

  @Test
  fun `authorize clients approved by user`() {
    val client = DataGenerator.buildClient(true, true)
    val ssoRequest = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      LocalDateTime.now(ZoneOffset.UTC),
      UUID.randomUUID(),
      SsoClient(
        client.id,
        "random_state",
        "random_nonce",
        setOf(Scope.USER_BASIC_READ),
        "http://localhost:8080/test",
      ),
      UUID.randomUUID().toString(),
    )
    Mockito.`when`(ssoLoginService.updateSsoRequest(null, ssoRequest.id))
      .thenReturn(RedirectView("${ssoRequest.client.redirectUri}?code=${ssoRequest.authorizationCode}&state=${ssoRequest.client.state}"))
    val redirectView = authController.authorizeClient(ssoRequest.id, "approved", ssoRequest.client.redirectUri) as RedirectView
    assertNotNull(redirectView)
    assertEquals(
      redirectView.url,
      "${ssoRequest.client.redirectUri}?code=${ssoRequest.authorizationCode}&state=${ssoRequest.client.state}",
    )
  }

  @Test
  fun `authorize clients for user approval declined`() {
    val client = DataGenerator.buildClient(true, true)
    val ssoRequest = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
      LocalDateTime.now(ZoneOffset.UTC),
      UUID.randomUUID(),
      SsoClient(client.id, "random_state", "random_nonce", setOf(Scope.USER_BASIC_READ), "http://localhost:8080/test"),
      UUID.randomUUID().toString(),
    )
    Mockito.`when`(ssoLoginService.updateSsoRequest(null, ssoRequest.id))
      .thenReturn("${ssoRequest.client.redirectUri}?code=${ssoRequest.authorizationCode}&state=${ssoRequest.client.state}")
    val exception = assertThrows(ApiException::class.java) {
      authController.authorizeClient(ssoRequest.id, "cancelled", ssoRequest.client.redirectUri)
    }
    assertEquals(302, exception.code)
  }
}
