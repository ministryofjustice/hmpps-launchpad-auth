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
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.time.Instant
import java.util.*

const val TEST_USER_ID = "testuser@test.com"

@SpringBootTest(classes = [IdTokenProcessor::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class IdTokenProcessorTest(@Autowired private var idTokenProcessor: IdTokenProcessor) {

  @Test
  fun getUserId() {
    val nonce = UUID.randomUUID()
    val userId = idTokenProcessor.getUserId(DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce), nonce.toString())
    assertEquals(TEST_USER_ID, userId)
  }

  @Test
  fun getUserIdWhenExpiryTimeInPast() {
    val nonce = UUID.randomUUID()
    val exception = assertThrows(ApiException::class.java) {
      idTokenProcessor.getUserId(DataGenerator.jwtBuilder(Instant.now(), Instant.now().minusSeconds(3600), nonce), nonce.toString())
    }
    assertEquals(ACCESS_DENIED, exception.message)
    assertEquals(ACCESS_DENIED_CODE, exception.code)
  }

  @Test
  fun getUserIdWhenNonceNotMatch() {
    val nonce = UUID.randomUUID()
    val exception = assertThrows(ApiException::class.java) {
      idTokenProcessor.getUserId(
        DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce),
        UUID.randomUUID().toString(),
      )
    }
    assertEquals(ACCESS_DENIED, exception.message)
    assertEquals(ACCESS_DENIED_CODE, exception.code)
  }
}
