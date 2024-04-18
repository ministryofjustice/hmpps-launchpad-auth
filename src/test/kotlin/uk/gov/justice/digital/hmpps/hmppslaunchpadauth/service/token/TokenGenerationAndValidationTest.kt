package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import java.util.*

class TokenGenerationAndValidationTest {

  private lateinit var claims: LinkedHashMap<String, Any>
  private lateinit var header: LinkedHashMap<String, Any>
  private lateinit var kid: String

  @BeforeEach
  fun setUp() {
    kid = UUID.randomUUID().toString()
    claims = LinkedHashMap()
    claims["firstName"] = "Test"
    claims["lastName"] = "User"
    header = TokenCommonClaims.buildHeaderClaims(kid)
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun testGenerateTokenWithSignature() {
    val keyPair = DataGenerator.generateRandomRSAKey()
    val privateKey = Base64.getEncoder().encodeToString(keyPair.private.encoded)
    val publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)
    val token = TokenGenerationAndValidation.generateJwtToken(claims, header, privateKey)
    val result = TokenGenerationAndValidation.validateJwtTokenSignature(token, publicKey)
    assertEquals(true, result)
  }

  @Test
  fun testVerifyToken() {
    val keyPair = DataGenerator.generateRandomRSAKey()
    val privateKey = Base64.getEncoder().encodeToString(keyPair.private.encoded)
    val publicKey = Base64.getEncoder().encodeToString(DataGenerator.generateRandomRSAKey().public.encoded)
    val token = TokenGenerationAndValidation.generateJwtToken(claims, header, privateKey)
    val result = TokenGenerationAndValidation.validateJwtTokenSignature(token, publicKey)
    assertEquals(false, result)
  }
}
