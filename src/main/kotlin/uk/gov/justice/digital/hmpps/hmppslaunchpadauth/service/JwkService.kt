package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenGenerationAndValidation
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Service("jwkService")
class JwkService(
  @Value("\${launchpad.auth.public-key}") private val publicKey: String,
  @Value("\${launchpad.auth.kid}") private val keyId: String,
) {

  fun getJwk(): MutableMap<String?, Any?> {
    val rsaPublicKey: RSAPublicKey = convertStringToRSAPublicKey(publicKey)
    val builder = RSAKey.Builder(rsaPublicKey)
      .keyUse(KeyUse.SIGNATURE)
      .algorithm(JWSAlgorithm.RS256)
      .keyID(keyId)
    return JWKSet(builder.build()).toJSONObject()
  }

  @Throws(Exception::class)
  private fun convertStringToRSAPublicKey(publicKeyString: String): RSAPublicKey {
    // Remove PEM headers if present
    var publicKeyString = publicKeyString
    publicKeyString = publicKeyString
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replace("-----END PUBLIC KEY-----", "")
      .replace("\\s+".toRegex(), "")

    // Decode Base64
    val decoded = Base64.getDecoder().decode(publicKeyString)

    // Create KeyFactory and generate public key
    val keySpec = X509EncodedKeySpec(decoded)
    val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
    val publicKey: PublicKey = keyFactory.generatePublic(keySpec)
    return publicKey as RSAPublicKey
  }

  fun jwkToRsaPublicKey(n: String, e: String): RSAPublicKey {
    val modulus = BigInteger(1, base64UrlDecode(n))
    val exponent = BigInteger(1, base64UrlDecode(e))
    val keySpec = RSAPublicKeySpec(modulus, exponent)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePublic(keySpec) as RSAPublicKey
  }

  private fun base64UrlDecode(data: String): ByteArray = Base64.getUrlDecoder().decode(data)

  fun rsaPublicKeyToBase64String(publicKey: RSAPublicKey) = Base64.getEncoder().encodeToString(publicKey.encoded)

  fun validateTokenWithPublicKey(token: String) = TokenGenerationAndValidation.validateJwtTokenSignature(token, publicKey)
}
