package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.UNAUTHORIZED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.UNAUTHORIZED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoRequestService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.PrisonerApiService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.net.URI
import java.util.*

const val TOKEN_TYPE = "Bearer"

@Component
class TokenService(
  private var prisonerApiService: PrisonerApiService,
  private var clientService: ClientService,
  private var ssoRequestService: SsoRequestService,
  private var userApprovedClientService: UserApprovedClientService,
  private var userIdValidator: UserIdValidator,
) {
  @Value("\${auth.service.secret}")
  private lateinit var secret: String
  private val logger = LoggerFactory.getLogger(TokenService::class.java)

  fun validateRequestAndGenerateToken(
    code: UUID?,
    grantType: String,
    redirectUri: URI?,
    clientId: UUID,
    refreshToken: String?,
    authenticationInfo: AuthenticationInfo,
    nonce: String?,
  ): Token {
    val client = clientService.getClientById(clientId)
      .orElseThrow {
        logger.warn("Client with id {} not found", clientId)
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED)
      }
    if (code != null && grantType == AuthorizationGrantType.AUTHORIZATION_CODE.toString() && redirectUri != null) {
      validateGrant(grantType, client.authorizedGrantTypes)
      val ssoRequest = ssoRequestService.getSsoRequestByAuthorizationCode(code).orElseThrow {
        logger.warn("Sso Request with code {} not found", code)
        throw ApiException("The code is invalid", 400, ApiErrorTypes.INVALID_CODE.toString(), "Invalid code")
      }
      validateRedirectUri(redirectUri, ssoRequest)
      val token = generateToken(ssoRequest.userId!!, clientId, ssoRequest.client.scopes, ssoRequest.client.nonce, null)
      ssoRequestService.deleteSsoRequestById(ssoRequest.id)
      return token
    } else if (refreshToken != null && grantType == "refresh_token") {
      validateGrant(grantType, client.authorizedGrantTypes)
      val refreshTokenPayloadOld = validateAndGetRefreshTokenPayloadClaims(refreshToken, clientId)
      val userId = refreshTokenPayloadOld.body["sub"] as String
      val scopes = refreshTokenPayloadOld.body["scopes"] as Any
      val userApprovedClient =
        userApprovedClientService.getUserApprovedClientByUserIdAndClientId(userId, clientId).orElseThrow {
          logger.warn("User approved client  with user id {} and client id {} not found", userId, clientId)
          throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED)
        }
      validateScopeInClaims(scopes, userApprovedClient.scopes)
      return generateToken(userId, clientId, userApprovedClient.scopes, nonce, refreshTokenPayloadOld)
    } else {
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED)
    }
  }

  private fun generateToken(
    prisonerId: String,
    clientId: UUID,
    scopes: Set<Scope>,
    nonce: String?,
    refreshTokenPayloadOld: Jws<Claims>?,
  ): Token {
    val prisonerData = prisonerApiService.getPrisonerData(prisonerId)
    val idTokenPayload = IdTokenPayload()
    val idTokenPayloadClaims = idTokenPayload.generatePayload(
      prisonerData.booking,
      prisonerData.establishment,
      prisonerData.profile,
      clientId,
      scopes,
      nonce,
    )
    val accessTokenPayload = AccessTokenPayload()
    val accessTokenPayloadClaims = accessTokenPayload.generatePayload(
      null,
      null,
      prisonerData.profile,
      clientId,
      scopes,
      nonce,
    )
    var refreshTokenPayloadClaims = LinkedHashMap<String, Any>()
    if (refreshTokenPayloadOld != null) {
      val claims = refreshTokenPayloadOld.body
      claims.keys.forEach { value ->
        val v = value as String
        refreshTokenPayloadClaims[v] = claims[value] as Any
      }
    } else {
      val refreshTokenPayload = RefreshTokenPayload()
      refreshTokenPayloadClaims = refreshTokenPayload.generatePayload(
        null,
        null,
        prisonerData.profile,
        clientId,
        scopes,
        nonce,
      )
    }
    val idToken = TokenGenerationAndValidation
      .generateToken(
        idTokenPayloadClaims,
        TokenCommonClaims.buildHeaderClaims(SignatureAlgorithm.HS256.name, "JWT"),
        secret,
      )
    val accessToken = TokenGenerationAndValidation
      .generateToken(
        accessTokenPayloadClaims,
        TokenCommonClaims.buildHeaderClaims(SignatureAlgorithm.HS256.name, "JWT"),
        secret,
      )
    refreshTokenPayloadClaims["ati"] = accessTokenPayloadClaims["jti"] as String
    val refreshToken = TokenGenerationAndValidation
      .generateToken(
        refreshTokenPayloadClaims,
        TokenCommonClaims.buildHeaderClaims(SignatureAlgorithm.HS256.name, "JWT"),
        secret,
      )
    return Token(idToken, accessToken, refreshToken, TOKEN_TYPE, 3600L)
  }

  private fun validateGrant(grantType: String, authorizationGrantType: Set<AuthorizationGrantType>) {
    val grant = AuthorizationGrantType.getAuthorizationGrantTypeByStringValue(grantType)
    if (!authorizationGrantType.contains(grant)) {
      logger.warn("Invalid grant type {} sent in get token request", grantType)
      throw ApiException("Invalid grant", 400, ApiErrorTypes.INVALID_GRANT.toString(), "Invalid grant")
    }
  }

  private fun validateRedirectUri(redirectUri: URI, ssoRequest: SsoRequest) {
    if (ssoRequest.client.redirectUri != redirectUri.toString()) {
      logger.warn("Redirect uri sent in token request not in list {}", redirectUri)
      throw ApiException("Invalid redirect uri", 400, ApiErrorTypes.INVALID_REDIRECT_URI.toString(), "Invalid redirect uri")
    }
  }

  private fun validateExpireTime(exp: Int) {
    try {
      TokenGenerationAndValidation.validateExpireTime(exp)
    } catch (e: IllegalArgumentException) {
      throw ApiException("Refresh token is expired", 400, ApiErrorTypes.INVALID_REQUEST.toString(), "Expired token")
    }
  }

  private fun getClaim(claimName: String, claims: Claims): Any? {
    try {
      return claims[claimName]
    } catch (e: NullPointerException) {
      logger.warn("Required claim {} not found in refresh token query parameter", claimName)
      throw ApiException(
        String.format("Missing claim %s in refresh token", claimName),
        400,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        "Invalid token"
      )
    }
  }

  private fun validateAndGetRefreshTokenPayloadClaims(refreshToken: String, clientId: UUID): Jws<Claims> {
    if (TokenGenerationAndValidation.validateJwtTokenSignature(refreshToken, secret)) {
      val claims = TokenGenerationAndValidation.parseClaims(refreshToken, secret)
      val exp = getClaim("exp", claims.body) as Int
      validateExpireTime(exp)
      val userIdInRefreshToken = getClaim("sub", claims.body) as String
      val clientIdInRefreshToken = getClaim("aud", claims.body) as String
      if (!userIdValidator.isValid(userIdInRefreshToken)) {
        logger.warn("Sub in refresh token do not match valid regex")
        throw ApiException("Invalid format of sub claim in refresh token", 400, ApiErrorTypes.INVALID_REQUEST.toString(), "invalid token")
      }
      if (clientId.toString() != clientIdInRefreshToken) {
        logger.debug(
          "client id {} from auth header do not match with aud {} in refresh token",
          clientId,
          clientIdInRefreshToken,
        )
        throw ApiException("Invalid aud claim in refresh token", 400, ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid request")
      }
      return claims
    } else {
      logger.warn("Refresh token signature is invalid")
      throw ApiException("Invalid refresh token", 400, ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid refresh token")
    }
  }

  private fun validateScopeInClaims(scopeInClaims: Any, scopeApprovedByUser: Set<Scope>) {
    val scopes = scopeInClaims as List<String>
    scopes.forEach { scope ->
      if (!Scope.isStringMatchEnumValue(scope, scopeApprovedByUser)) {
        throw ApiException("Permission denied", 403, ApiErrorTypes.ACCESS_DENIED.toString(), "Permission denied")
      }
    }
  }
}
