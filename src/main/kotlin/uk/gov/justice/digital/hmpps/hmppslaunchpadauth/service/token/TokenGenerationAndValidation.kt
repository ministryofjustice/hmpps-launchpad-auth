package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.jackson.io.JacksonSerializer
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class TokenGenerationAndValidation {

  companion object {
    private val mapper = Jackson2ObjectMapperBuilder()

    fun generateJwtToken(
      payloadMap: HashMap<String, Any>,
      headerMap: HashMap<String, Any>,
      secret: String,
    ): String {
      try {
        val privateKey = getPrivateKey(secret)
        return Jwts.builder()
          .serializeToJsonWith(JacksonSerializer(mapper.build()))
          .addClaims(payloadMap)
          .setHeader(headerMap)
          .signWith(privateKey, SignatureAlgorithm.RS256)
          .compact()
      } catch (e: Exception) {
        val message = "Exception during token creation ${e.message}"
        throw ApiException(
          message,
          HttpStatus.INTERNAL_SERVER_ERROR,
          ApiErrorTypes.SERVER_ERROR.toString(),
          ApiErrorTypes.SERVER_ERROR.toString(),
        )
      }
    }

    fun validateJwtTokenSignature(token: String, secret: String): Boolean {
      try {
        if (!token.contains(".")) {
          invalidTokenFormat(token)
        }
        val chunks = token.split(".")
        if (chunks.size != 3) {
          invalidTokenFormat(token)
        }
        val publicKey = getPublicKey(secret)
        return DefaultJwtSignatureValidator(SignatureAlgorithm.RS256, publicKey, Decoders.BASE64URL).isValid(
          chunks[0] + "." + chunks[1],
          chunks[2],
        )
      } catch (e: Exception) {
        val message = "Exception during token verification ${e.message}"
        throw ApiException(
          message,
          HttpStatus.FORBIDDEN,
          ApiErrorTypes.INVALID_TOKEN.toString(),
          ApiErrorTypes.INVALID_TOKEN.toString(),
        )
      }
    }

    fun parseClaims(token: String, secret: String): Jws<Claims> {
      try {
        val publicKey = getPublicKey(secret)
        return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token)
      } catch (e: ExpiredJwtException) {
        val message = "Invalid $token"
        throw ApiException(
          message,
          HttpStatus.BAD_REQUEST,
          ApiErrorTypes.INVALID_TOKEN.toString(),
          ApiErrorTypes.INVALID_TOKEN.toString(),
        )
      }
    }

    fun validateExpireTime(expireAt: Int) {
      val currentEpocTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
      if (currentEpocTime > expireAt) {
        throw IllegalArgumentException("Token has expired")
      }
    }

    private fun invalidTokenFormat(token: String) {
      val message = "Invalid bearer token format $token"
      throw ApiException(message, HttpStatus.FORBIDDEN, ApiErrorTypes.INVALID_TOKEN.toString(), "Invalid token")
    }

    private fun getPrivateKey(privateKey: String): PrivateKey {
      try {
        val privateKeyFormatted = privateKey
          .trimIndent()
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replace("-----END PRIVATE KEY-----", "")
          .replace("\\s".toRegex(), "")
        val privateKeyInBytes = Base64.getDecoder().decode(privateKeyFormatted)
        return KeyFactory.getInstance("RSA").generatePrivate(
          PKCS8EncodedKeySpec(privateKeyInBytes),
        )
      } catch (e: Exception) {
        val message = "Error converting private key string to private key object ${e.message}"
        throw ApiException(
          message,
          HttpStatus.INTERNAL_SERVER_ERROR,
          ApiErrorTypes.SERVER_ERROR.toString(),
          ApiErrorTypes.SERVER_ERROR.toString(),
        )
      }
    }

    private fun getPublicKey(publicKey: String): PublicKey {
      try {
        val publicKeyFormatted = publicKey
          .trimIndent()
          .replace("-----BEGIN PUBLIC KEY-----", "")
          .replace("-----END PUBLIC KEY-----", "")
          .replace("\\s".toRegex(), "")
        val publicKeyInBytes = Base64.getDecoder().decode(publicKeyFormatted)
        return KeyFactory.getInstance("RSA").generatePublic(
          X509EncodedKeySpec(publicKeyInBytes),
        )
      } catch (e: Exception) {
        val message = "Error converting public key string to public key object ${e.message}"
        throw ApiException(
          message,
          HttpStatus.INTERNAL_SERVER_ERROR,
          ApiErrorTypes.SERVER_ERROR.toString(),
          ApiErrorTypes.SERVER_ERROR.toString(),
        )
      }
    }
  }
}
