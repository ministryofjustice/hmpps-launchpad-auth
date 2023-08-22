package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.EXPIRE_TOKEN_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_TOKEN_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.UNAUTHORIZED_MSG
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
  @Value("\${auth.service.secret}")
  private lateinit var secret: String


  companion object {
    private const val BEARER = "Bearer "
    private val logger = LoggerFactory.getLogger(TokenAuthentication::class.java)
  }

  override fun authenticate(credential: String): AuthenticationInfo {
    if (credential.startsWith(BEARER)) {
      val token = credential.replace(BEARER, "")
      if (TokenGenerationAndValidation.validateJwtTokenSignature(token, secret)) {
        val claims = getJwsClaims(token)
        checkIfRefreshToken(claims)
        val expireAt = getClaim("exp", claims) as Int
        validateExpireTime(expireAt)
        TokenGenerationAndValidation.validateExpireTime(expireAt)
        val jti = getClaim("jti", claims) as String
        validateAndGetUUIDInClaim(jti, "jti")
        getClaim("iat", claims) as Int
        val aud = getClaim("aud", claims) as String
        val clientId = validateAndGetUUIDInClaim(aud, "aud")
        val userId = getClaim("sub", claims) as String
        validateUserIdFormat(userId)
        val scopes = getClaim("scopes", claims) as Any
        val scopesEnum = validateScopeInClaims(scopes)
        logger.info("Successful token authentication for client {} user id {}", clientId, userId)
        return AuthenticationUserInfo(clientId, userId, scopesEnum)
      } else {
        throw ApiException(UNAUTHORIZED_MSG, HttpStatus.UNAUTHORIZED.value(), ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED_MSG)
      }
    } else {
      throw ApiException(UNAUTHORIZED_MSG, HttpStatus.UNAUTHORIZED.value(), ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED_MSG)
    }
  }


  private fun getClaim(claimName: String, claims: Claims): Any? {
    val claim = claims[claimName]
    if (claim == null) {
      val message = String.format("Required claim %s not found in access token", claimName)
      throw ApiException(
        message,
        HttpStatus.UNAUTHORIZED.value(),
        ApiErrorTypes.UNAUTHORIZED.toString(),
        INVALID_TOKEN_MSG,
      )
    }
    return claim
  }

  private fun getJwsClaims(token: String): Claims {
    try {
      val jwsClaims = TokenGenerationAndValidation.parseClaims(token, secret)
      return jwsClaims.body
    } catch (e: JwtException) {
      val message = String.format("Exception during parsing claims in token authentication %s", e.message)
      throw ApiException(message, HttpStatus.UNAUTHORIZED.value(), ApiErrorTypes.INVALID_TOKEN.toString(), INVALID_TOKEN_MSG)
    }
  }

  private fun validateExpireTime(exp: Int) {
    try {
      TokenGenerationAndValidation.validateExpireTime(exp)
    } catch (e: IllegalArgumentException) {
      val message = String.format("Exception during token expire time validation {}", e.message)
      throw ApiException(
        message,
        HttpStatus.UNAUTHORIZED.value(),
        ApiErrorTypes.EXPIRED_ACCESS_TOKEN.toString(),
        EXPIRE_TOKEN_MSG,
      )
    }
  }

  private fun validateAndGetUUIDInClaim(value: String, claimName: String): UUID {
    try {
      return UUID.fromString(value)
    } catch (e: IllegalArgumentException) {
      val message = String.format("Exception during token authentication invalid UUID string in %s", claimName)
      throw ApiException(
        message,
        HttpStatus.UNAUTHORIZED.value(),
        ApiErrorTypes.UNAUTHORIZED.toString(),
        INVALID_TOKEN_MSG,
      )
    }
  }

  private fun validateUserIdFormat(sub: String) {
    if (!userIdValidator.isValid(sub)) {
      val message = String.format("Invalid user id %s format in token", sub)
      throw ApiException(
        message,
        HttpStatus.UNAUTHORIZED.value(),
        ApiErrorTypes.UNAUTHORIZED.toString(),
        INVALID_TOKEN_MSG,
      )
    }
  }

  private fun checkIfRefreshToken(claims: Claims) {
    val ati = claims["ati"]
    if (ati != null) {
      throw ApiException(
        "Refresh token sent as bearer auth token",
        HttpStatus.UNAUTHORIZED.value(),
        ApiErrorTypes.UNAUTHORIZED.toString(),
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
          val message = String.format("Scope %s in token not in auth service scope list", scope)
          throw ApiException(
            message,
            HttpStatus.UNAUTHORIZED.value(),
            ApiErrorTypes.UNAUTHORIZED.toString(),
            INVALID_TOKEN_MSG,
          )
        }
      }
      return scopeEnums
    } else {
      throw ApiException(
        "Access token do not contain scopes",
        HttpStatus.UNAUTHORIZED.value(),
        ApiErrorTypes.UNAUTHORIZED.toString(),
        INVALID_TOKEN_MSG,
      )
    }
  }
}
