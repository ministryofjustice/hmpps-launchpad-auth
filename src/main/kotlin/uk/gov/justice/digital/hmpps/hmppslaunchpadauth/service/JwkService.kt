package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INTERNAL_SERVER_ERROR_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
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

  private fun convertStringToRSAPublicKey(publicKeyString: String): RSAPublicKey {
    try {
      var publicKeyPEM = publicKeyString
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace(System.lineSeparator(), "")
        .replace("-----END PUBLIC KEY-----", "")
        .replace("\\s+".toRegex(), "")

      val decoded = Base64.getDecoder().decode(publicKeyPEM)
      val keySpec = X509EncodedKeySpec(decoded)
      val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
      val publicKey: PublicKey = keyFactory.generatePublic(keySpec)
      return publicKey as RSAPublicKey
    } catch (e: NoSuchAlgorithmException) {
      throw ApiException("NoSuchAlgorithm", HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    } catch (e: InvalidKeySpecException) {
      throw ApiException("InvalidKeySpec", HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    }
  }

  fun jwkToRsaPublicKey(n: String, e: String): RSAPublicKey {
    val modulus = BigInteger(1, base64UrlDecode(n))
    val exponent = BigInteger(1, base64UrlDecode(e))
    val keySpec = RSAPublicKeySpec(modulus, exponent)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePublic(keySpec) as RSAPublicKey
  }

  private fun base64UrlDecode(data: String): ByteArray = Base64.getUrlDecoder().decode(data)

  fun rsaPublicKeyToBase64String(publicKey: RSAPublicKey): String? = Base64.getEncoder().encodeToString(publicKey.encoded)
}
