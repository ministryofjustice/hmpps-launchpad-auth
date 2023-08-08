package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.UNAUTHORIZED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.UNAUTHORIZED_CODE
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.crypto.spec.SecretKeySpec

class TokenGenerationAndValidation {

  companion object {
    private val logger = LoggerFactory.getLogger(TokenGenerationAndValidation::class.java)
    fun createToken(
      payloadMap: HashMap<String, Any>,
      headerMap: HashMap<String, Any>,
      alg: SignatureAlgorithm,
      secret: String,
    ): String {
      try {
        return Jwts.builder()
          .addClaims(payloadMap)
          .setHeader(headerMap)
          .signWith(alg, secret.toByteArray(Charsets.UTF_8))
          .compact()
      } catch (e: Exception) {
        logger.debug("Exception during token creation {}", e.message)
        throw ApiException("Exception during token creation ", HttpStatus.INTERNAL_SERVER_ERROR.value())
      }
    }

    fun validateJwtTokenSignature(token: String, secret: String): Boolean {
      if (!token.contains(".")) {
        invalidTokenFormat(token)
      }
      val chunks = token.split(".")
      if (chunks.size != 3) {
        invalidTokenFormat(token)
      }
      val secretKeySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), SignatureAlgorithm.HS256.value)
      return DefaultJwtSignatureValidator(SignatureAlgorithm.HS256, secretKeySpec).isValid(
        chunks[0] + "." + chunks[1],
        chunks[2],
      )
    }

    fun parseClaims(token: String, secret: String): Jws<Claims> {
      return Jwts.parser().setSigningKey(secret.toByteArray(Charsets.UTF_8)).parseClaimsJws(token)
    }

    fun validateExpireTime(expireAt: Int) {
      val currentEpocTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
      if (currentEpocTime > expireAt) {
        throw IllegalArgumentException("Token has expired")
      }
    }

    private fun invalidTokenFormat(token: String) {
      logger.error("Invalid bearer token format {}", token)
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }
}
