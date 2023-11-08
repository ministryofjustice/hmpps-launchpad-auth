package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.time.Instant
import java.util.*

@SpringBootTest(classes = [IdTokenProcessor::class, UserIdValidator::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class IdTokenProcessorTest(@Autowired private var idTokenProcessor: IdTokenProcessor) {

  @Value("\${launchpad.auth.secret}")
  private lateinit var secret: String

  @Test
  fun `test get user id when nonce match`() {
    val nonce = UUID.randomUUID()
    val userUniqueId = "G2320VD"
    var userId = idTokenProcessor.getUserId(DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, userUniqueId, secret), nonce.toString())
    assertEquals(userId, userId)
  }

  @Test
  fun `test get user id when email is missing in payload`() {
    val nonce = UUID.randomUUID()
    val exception = assertThrows(ApiException::class.java) {
      idTokenProcessor.getUserId(
        DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, null, secret),
        nonce.toString(),
      )
    }
    assertEquals("Claim: email not found", exception.message)
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.code)
  }

  @Test
  fun `test get user id when nonce do not match`() {
    val nonce = UUID.randomUUID()
    assertThrows(IllegalArgumentException::class.java) {
      idTokenProcessor.getUserId(
        DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, "test@moj.com", secret),
        UUID.randomUUID().toString(),
      )
    }
  }
}
