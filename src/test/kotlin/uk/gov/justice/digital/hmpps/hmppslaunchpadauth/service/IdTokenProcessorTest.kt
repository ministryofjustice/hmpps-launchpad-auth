package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import java.time.Instant
import java.util.*

@SpringBootTest(classes = [IdTokenProcessor::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class IdTokenProcessorTest(@Autowired private var idTokenProcessor: IdTokenProcessor) {

  @Test
  fun `test get user id when nonce match`() {
    val nonce = UUID.randomUUID()
    val userUniqueId = "G2320VD"
    var userId = idTokenProcessor.getUserId(DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, userUniqueId), nonce.toString())
    assertEquals(userId, userId)
  }

  @Test
  fun `test get user id when email is missing in payload`() {
    val nonce = UUID.randomUUID()
    val exception = assertThrows(ApiException::class.java) {
      idTokenProcessor.getUserId(
        DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, null),
        nonce.toString(),
      )
    }
    assertEquals("Claim: email not found", exception.message)
    assertEquals(500, exception.code)
  }

  @Test
  fun `test get user id when nonce do not match`() {
    val nonce = UUID.randomUUID()
    val exception = assertThrows(ApiException::class.java) {
      idTokenProcessor.getUserId(
        DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, "test@moj.com"),
        UUID.randomUUID().toString(),
      )
    }
    assertEquals(ACCESS_DENIED, exception.message)
    assertEquals(ACCESS_DENIED_CODE, exception.code)
  }
}
