package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.BaseIntegrationTest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.time.Instant
import java.util.*

@SpringBootTest(classes = [IdTokenProcessor::class, UserIdValidator::class])
@ActiveProfiles("test")
class IdTokenProcessorTest(@Autowired private var idTokenProcessor: IdTokenProcessor) : BaseIntegrationTest() {

  @Value("\${launchpad.auth.private-key}")
  private lateinit var privateKey: String

  @Test
  fun `test get user id when nonce match`() {
    val nonce = UUID.randomUUID()
    val userUniqueId = "G2320VD"
    var userId = idTokenProcessor.getUserId(DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, userUniqueId, privateKey), nonce.toString())
    assertEquals(userId, userId)
  }

  @Test
  fun `test get user id when email is missing in payload`() {
    val nonce = UUID.randomUUID()
    val token = DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, null, privateKey)
    val exception = assertThrows(ApiException::class.java) {
      idTokenProcessor.getUserId(
        token,
        nonce.toString(),
      )
    }
    assertEquals("Claim: preferred_username not found in token:$token", exception.message)
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.code)
  }

  @Test
  fun `test get user id when nonce do not match`() {
    val nonce = UUID.randomUUID()
    assertThrows(IllegalArgumentException::class.java) {
      idTokenProcessor.getUserId(
        DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, "test@moj.com", privateKey),
        UUID.randomUUID().toString(),
      )
    }
  }
}
