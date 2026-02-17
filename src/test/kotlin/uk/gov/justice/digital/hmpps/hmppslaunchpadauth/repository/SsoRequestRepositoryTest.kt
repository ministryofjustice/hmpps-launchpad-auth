package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config.TestCacheConfig
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.BaseIntegrationTest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import java.util.UUID

@DataJpaTest()
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository"])
@EntityScan("uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model")
@ActiveProfiles("test")
@Import(TestCacheConfig::class)
class SsoRequestRepositoryTest(@Autowired private var ssoRequestRepository: SsoRequestRepository) : BaseIntegrationTest() {
  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
    ssoRequestRepository.deleteAll()
  }

  @Test
  fun `create sso request`() {
    val expected = DataGenerator.buildSsoRequest()
    val result = ssoRequestRepository.save(expected)
    assertSsoRequest(expected, result)
  }

  @Test
  fun `update sso request`() {
    val expected = DataGenerator.buildSsoRequest()
    expected.authorizationCode = UUID.randomUUID()
    val result = ssoRequestRepository.save(expected)
    assertSsoRequest(expected, result)
  }

  @Test
  fun `get sso request by id`() {
    val expected = DataGenerator.buildSsoRequest()
    expected.authorizationCode = UUID.randomUUID()
    ssoRequestRepository.save(expected)
    val result = ssoRequestRepository.findById(expected.id)
    assertSsoRequest(expected, result.get())
  }

  @Test
  fun `delete sso request by id`() {
    val expected = DataGenerator.buildSsoRequest()
    expected.authorizationCode = UUID.randomUUID()
    ssoRequestRepository.save(expected)
    ssoRequestRepository.deleteById(expected.id)
    assertEquals(false, ssoRequestRepository.findById(expected.id).isPresent)
  }

  private fun assertSsoRequest(expected: SsoRequest, result: SsoRequest) {
    assertEquals(expected.id, result.id)
    // assertEquals(expected.createdDate, result.createdDate)
    assertEquals(expected.authorizationCode, result.authorizationCode)
    assertEquals(expected.nonce, result.nonce)
    assertEquals(expected.userId, result.userId)
    assertSsoClient(expected.client, result.client)
  }

  private fun assertSsoClient(expected: SsoClient, result: SsoClient) {
    assertEquals(expected.id, result.id)
    assertEquals(expected.state, result.state)
    assertEquals(expected.scopes, result.scopes)
    assertEquals(expected.redirectUri, result.redirectUri)
    assertEquals(expected.nonce, result.nonce)
  }
}
