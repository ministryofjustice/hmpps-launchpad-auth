package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.crypto.spec.SecretKeySpec

class TokenGeneration {
  // private val logger = LoggerFactory.getLogger(TokenGeneration::class.java)
  companion object {
    fun createToken(
      payloadMap: HashMap<String, Any>,
      headerMap: HashMap<String, Any>,
      alg: SignatureAlgorithm,
      secret: String
    ): String {
      return Jwts.builder()
        .addClaims(payloadMap)
        .setHeader(headerMap)
        .signWith(alg, secret.toByteArray(Charsets.UTF_8))
        .compact()
    }

    fun validateJwtTokenSignature(token: String, secret: String): Boolean {
      val decoder = Base64.getUrlDecoder()
      val chunks = token.split(".")
      val header  = String(decoder.decode(chunks[0]))
      val payload = String(decoder.decode(chunks[1]))
      val signature = String(decoder.decode(chunks[2]))
      val secretKeySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), SignatureAlgorithm.HS256.value)
      return DefaultJwtSignatureValidator(SignatureAlgorithm.HS256, secretKeySpec).isValid(chunks[0] + "." + chunks[1], chunks[2])
    }

    fun parseClaims(token: String, secret: String): Jws<Claims> {
      return Jwts.parser().setSigningKey(secret.toByteArray(Charsets.UTF_8)).parseClaimsJws(token)
    }

    private fun validateExpireTime(expireAt: Long) {
      val currentEpocTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
      if (currentEpocTime > expireAt) {
        throw IllegalArgumentException("Token has expired")
      }
    }
  }
}