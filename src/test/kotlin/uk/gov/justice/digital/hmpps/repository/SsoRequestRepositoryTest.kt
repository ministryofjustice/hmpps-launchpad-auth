package uk.gov.justice.digital.hmpps.repository

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.util.*

@SpringBootTest(classes = [SsoRequestRepository::class])
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository"])
@EntityScan("uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model")
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class SsoRequestRepositoryTest(@Autowired private var ssoRequestRepository: SsoRequestRepository) {
  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun `create ssorequest`() {
    val expected = DataGenerator.buildSsoRequest()
    val result = ssoRequestRepository.save(expected)
    assertSsoRequest(expected, result)
  }

  @Test
  fun `update ssorequest`() {
    val expected = DataGenerator.buildSsoRequest()
    expected.authorizationCode = UUID.randomUUID()
    val result = ssoRequestRepository.save(expected)
    assertSsoRequest(expected, result)
  }

  @Test
  fun `get ssorequest by id`() {
    val expected = DataGenerator.buildSsoRequest()
    expected.authorizationCode = UUID.randomUUID()
    ssoRequestRepository.save(expected)
    val result = ssoRequestRepository.findById(expected.id)
    assertSsoRequest(expected, result.get())
  }

  @Test
  fun `delete ssorequest by id`(){
    val expected = DataGenerator.buildSsoRequest()
    expected.authorizationCode = UUID.randomUUID()
    ssoRequestRepository.save(expected)
    ssoRequestRepository.deleteById(expected.id)
    assertEquals(false, ssoRequestRepository.findById(expected.id).isPresent)
  }

  private fun assertSsoRequest(expected: SsoRequest, result: SsoRequest) {
    assertEquals(expected.id, result.id)
    assertEquals(expected.createdDate, result.createdDate)
    assertEquals(expected.authorizationCode, result.authorizationCode)
    assertEquals(expected.nonce, result.nonce)
    assertEquals(expected.userId, result.userId)
    assertSsoClient(expected.client, result.client)
  }

  private fun assertSsoClient(expected: SsoClient, result: SsoClient) {
    assertEquals(expected.id, result.id)
    assertEquals(expected.state, result.state)
    assertEquals(expected.scopes, result.scopes)
    assertEquals(expected.reDirectUri, result.reDirectUri)
    assertEquals(expected.nonce, result.nonce)
  }

}