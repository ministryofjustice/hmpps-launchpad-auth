package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.ACCESS_DENIED_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.EXPIRE_TOKEN_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_TOKEN_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenGenerationAndValidation
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.util.*

@Service("tokenAuthentication")
class TokenAuthentication(
  private var userIdValidator: UserIdValidator,
) : Authentication {
  @Value("\${launchpad.auth.public-key}")
  private lateinit var publicKey: String

  companion object {
    private const val BEARER = "Bearer "
    private val logger = LoggerFactory.getLogger(TokenAuthentication::class.java)
  }

  override fun authenticate(credential: String): AuthenticationInfo {
    if (credential.startsWith(BEARER)) {
      val token = credential.replace(BEARER, "")
      if (TokenGenerationAndValidation.validateJwtTokenSignature(token, publicKey)) {
        val claims = getJwsClaims(token)
        checkIfRefreshToken(claims)
        val expireAt = getClaim("exp", claims) as Long
        validateExpireTime(expireAt)
        getClaim("jti", claims) as String
        getClaim("iat", claims) as Long
        val client = getClaim("aud", claims) as LinkedHashSet<Any>
        val aud = client.first as String
        val clientId = validateAndGetUUIDInClaim(aud, "aud")
        val userId = getClaim("sub", claims) as String
        val scopes = getClaim("scopes", claims)
        val scopesEnum = validateScopeInClaims(scopes)
        logger.info("Successful token authentication for client {} user id {}", clientId, userId)
        return AuthenticationUserInfo(clientId, userId, scopesEnum)
      } else {
        throw ApiException(
          INVALID_TOKEN_MSG,
          HttpStatus.FORBIDDEN,
          ApiErrorTypes.INVALID_TOKEN.toString(),
          INVALID_TOKEN_MSG,
        )
      }
    } else {
      throw ApiException(
        INVALID_TOKEN_MSG,
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.INVALID_TOKEN.toString(),
        INVALID_TOKEN_MSG,
      )
    }
  }

  private fun getClaim(claimName: String, claims: Claims): Any {
    val claim = claims[claimName]
    if (claim == null) {
      val message = "Required claim $claimName not found in access token"
      throw ApiException(
        message,
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.ACCESS_DENIED.toString(),
        INVALID_TOKEN_MSG,
      )
    }
    return claim
  }

  private fun getJwsClaims(token: String): Claims {
    try {
      val jwsClaims = TokenGenerationAndValidation.parseClaims(token, publicKey)
      return jwsClaims.payload
    } catch (e: JwtException) {
      val message = "Exception during parsing claims in token authentication ${e.message}"
      throw ApiException(
        message,
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.ACCESS_DENIED.toString(),
        INVALID_TOKEN_MSG,
      )
    }
  }

  private fun validateExpireTime(exp: Long) {
    try {
      TokenGenerationAndValidation.validateExpireTime(exp)
    } catch (e: IllegalArgumentException) {
      val message = "Exception during token expire time validation ${e.message}"
      throw ApiException(
        message,
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.EXPIRED_TOKEN.toString(),
        EXPIRE_TOKEN_MSG,
      )
    }
  }

  private fun validateAndGetUUIDInClaim(value: String, claimName: String): UUID {
    try {
      return UUID.fromString(value)
    } catch (e: IllegalArgumentException) {
      val message = "Exception during token authentication invalid UUID string in $claimName"
      throw ApiException(
        message,
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.ACCESS_DENIED.toString(),
        INVALID_TOKEN_MSG,
      )
    }
  }

  private fun checkIfRefreshToken(claims: Claims) {
    val ati = claims["ati"]
    if (ati != null) {
      throw ApiException(
        "Refresh token sent as bearer auth token",
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.ACCESS_DENIED.toString(),
        INVALID_TOKEN_MSG,
      )
    }
  }

  private fun validateScopeInClaims(scopeInClaims: Any): Set<Scope> {
    val scopes = scopeInClaims as? ArrayList<String>
    val scopeEnums = HashSet<Scope>()
    if (scopes != null) {
      scopes.forEach { scope ->
        try {
          val scopeEnum = Scope.getScopeByStringValue(scope)
          scopeEnums.add(scopeEnum)
        } catch (e: IllegalArgumentException) {
          val message = "Scope $scope in token not in auth service scope list"
          throw ApiException(
            message,
            HttpStatus.FORBIDDEN,
            ApiErrorTypes.ACCESS_DENIED.toString(),
            ACCESS_DENIED_MSG,
          )
        }
      }
      return scopeEnums
    } else {
      throw ApiException(
        "Access token do not contain scopes",
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.INVALID_TOKEN.toString(),
        INVALID_TOKEN_MSG,
      )
    }
  }
}
