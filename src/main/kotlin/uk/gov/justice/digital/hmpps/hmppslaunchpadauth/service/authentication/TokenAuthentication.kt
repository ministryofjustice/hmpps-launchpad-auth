package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.UNAUTHORIZED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.UNAUTHORIZED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenGenerationAndValidation
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


@Component("tokenAuthentication")
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
        try {
          val jwsClaims = TokenGenerationAndValidation.parseClaims(token, secret)
          val claims = jwsClaims.body
          val expireAt = getClaim("exp", claims) as Int
          validateExpireTime(expireAt)
          TokenGenerationAndValidation.validateExpireTime(expireAt)
          val jti = getClaim("jti", claims) as String
          validateAndGetUUIDInClaim(jti, "jti")
          val aud = getClaim("aud", claims) as String
          val clientId = validateAndGetUUIDInClaim(aud, "aud")
          val userId = getClaim("sub", claims) as String
          validateUserIdFormat(userId)
          val scopes = getClaim("scopes", claims) as Any
          val scopesEnum = validateScopeInClaims(scopes)
          return AuthenticationUserInfo(clientId, userId, scopesEnum)
        } catch (e: Exception) {
          when (e) {
            is ExpiredJwtException, is UnsupportedJwtException, is MalformedJwtException, is SignatureException, is IllegalArgumentException -> {
              logger.debug("Exception during bearer token authentication: {}", e.message)
              throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED,)
            }
            is Exception -> {
              throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED,)
            }
          }
        }
      } else {
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED,)
      }
    } else {
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED,)
    }
    throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED,)
  }


  private fun getClaim(claimName: String, claims: Claims): Any? {
    try {
      return claims[claimName]
    } catch (e: NullPointerException) {
      logger.warn("Required claim {} not found in refresh token query parameter", claimName)
      throw ApiException(
        "Invalid token",
        401,
        ApiErrorTypes.UNAUTHORIZED.toString(),
        "Invalid token",
      )
    }
  }

  private fun validateExpireTime(exp: Int) {
    try {
      TokenGenerationAndValidation.validateExpireTime(exp)
    } catch (e: IllegalArgumentException) {
      throw ApiException("Refresh token is expired", 400, ApiErrorTypes.INVALID_REQUEST.toString(), "Refresh token is expired",)
    }
  }

  private fun validateAndGetUUIDInClaim(aud: String, claimName: String): UUID {
    try {
      return UUID.fromString(aud)
    } catch (e: IllegalArgumentException) {
      logger.warn("Invalid claim name {} format in token", claimName)
      throw ApiException(
        "Invalid token",
        401,
        ApiErrorTypes.UNAUTHORIZED.toString(),
        "Invalid token"
      )
    }
  }

  private fun validateUserIdFormat(sub: String) {
    if (!userIdValidator.isValid(sub)) {
      logger.warn("Invalid user id format in token")
      throw ApiException(
        "Invalid token",
        401,
        ApiErrorTypes.UNAUTHORIZED.toString(),
        "Invalid token",
      )
    }
  }

  private fun validateScopeInClaims(scopeInClaims: Any): Set<Scope> {
    val scopes = scopeInClaims as? ArrayList<String>
    val scopeEnums = HashSet<Scope>()
    if (scopes != null) {
        scopes.forEach{
          scope ->
          try {
            val scopeEnum = Scope.getScopeByStringValue(scope)
            scopeEnums.add(scopeEnum)
          } catch (e: IllegalArgumentException) {
            throw ApiException("Token do not contain scopes", UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), "Invalid token",)
        }
      }
      return scopeEnums
    } else {
      throw ApiException("Token do not contain scopes", UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), "Invalid token",)
    }
  }
}
