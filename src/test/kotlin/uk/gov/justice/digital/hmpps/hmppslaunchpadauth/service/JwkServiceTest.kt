package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator.Companion.generateRandomRSAKey
import java.security.interfaces.RSAPublicKey
import java.util.*

@SpringBootTest(classes = [JwkService::class])
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class JwkServiceTest {

  private lateinit var service: JwkService

  private lateinit var randomPublicKey: String

  @Value("\${launchpad.auth.public-key}")
  private lateinit var launchpadPublicKey: String

  @Value("\${launchpad.auth.kid}")
  private lateinit var keyId: String

  @BeforeEach
  fun setUp() {
  }

  @AfterEach
  fun tearDown() {
  }

  @Test
  fun validateJwks() {
    service = JwkService(launchpadPublicKey, keyId)
    val jwks = service.getJwk()
    assert(jwks.keys.isNotEmpty())
    val keyset: ArrayList<Any> = jwks["keys"] as ArrayList<Any>
    val keys: HashMap<*, *> = keyset.first() as HashMap<*, *>
    val modulus = keys["n"]
    val exponent = keys["e"]
    assert(modulus != null && modulus is String && modulus.isNotEmpty())
    assert(exponent != null && exponent is String && exponent.isNotEmpty())
    val rsaPublicKey: RSAPublicKey = service.jwkToRsaPublicKey(modulus as String, exponent as String)
    val outputKey = service.rsaPublicKeyToBase64String(rsaPublicKey)
    assert(outputKey != null)
    assert(outputKey == launchpadPublicKey.replace("\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", ""))
  }

  @Test
  fun validateRandomJwks() {
    val keyPair = generateRandomRSAKey()
    randomPublicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)
    service = JwkService(randomPublicKey, keyId)
    val jwks = service.getJwk()
    assert(jwks.keys.isNotEmpty())
    val keyset: ArrayList<Any> = jwks["keys"] as ArrayList<Any>
    val keys: HashMap<*, *> = keyset.first() as HashMap<*, *>
    val modulus = keys["n"]
    val exponent = keys["e"]
    assert(modulus != null && modulus is String && modulus.isNotEmpty())
    assert(exponent != null && exponent is String && exponent.isNotEmpty())
    val rsaPublicKey: RSAPublicKey = service.jwkToRsaPublicKey(modulus as String, exponent as String)
    val outputKey = service.rsaPublicKeyToBase64String(rsaPublicKey)
    assert(outputKey != null)
    assert(outputKey == randomPublicKey.replace("\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", ""))
  }
}
