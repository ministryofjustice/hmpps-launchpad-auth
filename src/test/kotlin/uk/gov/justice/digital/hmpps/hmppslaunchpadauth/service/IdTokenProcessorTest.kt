package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.time.Instant

const val TEST_USER_ID = "vrnkmr110@outlook.com"

@SpringBootTest(classes = [IdTokenProcessor::class])
@EnableAutoConfiguration
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class IdTokenProcessorTest(@Autowired private var idTokenProcessor: IdTokenProcessor) {

  @Test
  fun getUserId() {
    val userId = idTokenProcessor.getUserId(DataGenerator.jwtBuilder(Instant.now(), Instant.now().plusSeconds(3600)))
    assertEquals(TEST_USER_ID, userId)
  }

  @Test
  fun getUserIdWhenExpiryTimeInPast() {
    val exception = assertThrows(ApiException::class.java) {
      idTokenProcessor.getUserId(DataGenerator.jwtBuilder(Instant.now(), Instant.now().minusSeconds(3600)))
    }
    assertEquals(403, exception.code)
  }
}