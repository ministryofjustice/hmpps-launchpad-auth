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



@SpringBootTest(classes = [IdTokenProcessor::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class IdTokenProcessorTest(@Autowired private var idTokenProcessor: IdTokenProcessor) {

  @Test
  fun getUserId() {
    val nonce = UUID.randomUUID()
    val id = UUID.randomUUID()
    val userId = idTokenProcessor.getUserId(DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, id), nonce.toString())
    assertEquals(userId, id.toString())
  }

  @Test
  fun getUserIdWhenNonceNotMatch() {
    val nonce = UUID.randomUUID()
    val exception = assertThrows(ApiException::class.java) {
      idTokenProcessor.getUserId(
        DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600), nonce, UUID.randomUUID()),
        UUID.randomUUID().toString(),
      )
    }
    assertEquals(ACCESS_DENIED, exception.message)
    assertEquals(ACCESS_DENIED_CODE, exception.code)
  }
}
