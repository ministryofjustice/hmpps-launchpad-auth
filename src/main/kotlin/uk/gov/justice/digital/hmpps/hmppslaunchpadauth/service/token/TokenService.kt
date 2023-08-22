package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.ACCESS_DENIED_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.EXPIRE_TOKEN_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_CODE_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_GRANT_TYPE_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_REDIRECT_URI_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_REQUEST_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.UNAUTHORIZED_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
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
        throw ApiException(
          UNAUTHORIZED_MSG,
          HttpStatus.UNAUTHORIZED.value(),
          ApiErrorTypes.UNAUTHORIZED.toString(),
          UNAUTHORIZED_MSG,
        )
      }
    if (code != null && grantType == AuthorizationGrantType.AUTHORIZATION_CODE.toString() && redirectUri != null) {
      return generateTokenByCode(code, grantType, redirectUri, client)
    } else if (refreshToken != null && grantType == AuthorizationGrantType.REFRESH_TOKEN.toString()) {
      return generateTokenByRefreshToken(refreshToken, grantType, client, nonce)
    } else {
      throw ApiException(
        INVALID_REQUEST_MSG,
        HttpStatus.BAD_REQUEST.value(),
        ApiErrorTypes.INVALID_REQUEST.toString(),
        INVALID_REQUEST_MSG,
      )
    }
  }

  private fun generateTokenByCode(code: UUID, grantType: String, redirectUri: URI, client: Client): Token {
    validateGrant(grantType, client.authorizedGrantTypes)
    val ssoRequest = ssoRequestService.getSsoRequestByAuthorizationCode(code)
      .orElseThrow {
        logger.warn("Sso Request with code {} not found", code)
        throw ApiException("The code is invalid", 400, ApiErrorTypes.INVALID_CODE.toString(), INVALID_CODE_MSG)
      }
    validateRedirectUri(redirectUri, ssoRequest)
    val token = generateToken(ssoRequest.userId!!, client.id, ssoRequest.client.scopes, ssoRequest.client.nonce, null)
    ssoRequestService.deleteSsoRequestById(ssoRequest.id)
    return token
  }

  private fun generateTokenByRefreshToken(
    refreshToken: String,
    grantType: String,
    client: Client,
    nonce: String?,
  ): Token {
    validateGrant(grantType, client.authorizedGrantTypes)
    val refreshTokenPayloadOld = validateAndGetRefreshTokenPayloadClaims(refreshToken, client.id)
    val userId = refreshTokenPayloadOld.body["sub"] as String
    val scopes = refreshTokenPayloadOld.body["scopes"] as Any
    val userApprovedClient =
      userApprovedClientService.getUserApprovedClientByUserIdAndClientId(userId, client.id)
        .orElseThrow {
        logger.warn("User approved client  with user id {} and client id {} not found", userId, client.id)
        throw ApiException(
          UNAUTHORIZED_MSG,
          HttpStatus.UNAUTHORIZED.value(),
          ApiErrorTypes.UNAUTHORIZED.toString(),
          UNAUTHORIZED_MSG,
        )
      }
    validateScopeInClaims(scopes, userApprovedClient.scopes)
    return generateToken(userId, client.id, userApprovedClient.scopes, nonce, refreshTokenPayloadOld)
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
      val message = String.format("Invalid grant type %s sent in get token request", grantType)
      logger.warn(message)
      throw ApiException(message, 400, ApiErrorTypes.INVALID_GRANT.toString(), INVALID_GRANT_TYPE_MSG)
    }
  }

  private fun validateRedirectUri(redirectUri: URI, ssoRequest: SsoRequest) {
    if (ssoRequest.client.redirectUri != redirectUri.toString()) {
      val message = String.format("Redirect uri sent in token request not in list %s", redirectUri)
      logger.warn(message)
      throw ApiException(message, 400, ApiErrorTypes.INVALID_REDIRECT_URI.toString(), INVALID_REDIRECT_URI_MSG)
    }
  }

  private fun validateExpireTime(exp: Int) {
    try {
      TokenGenerationAndValidation.validateExpireTime(exp)
    } catch (e: IllegalArgumentException) {
      throw ApiException(
        "Expired refresh token",
        400,
        ApiErrorTypes.EXPIRED_REFRESH_TOKEN.toString(),
        EXPIRE_TOKEN_MSG,
      )
    }
  }

  private fun getClaim(claimName: String, claims: Claims): Any {
    val claim = claims[claimName]
    if (claim == null) {
      val message = String.format("Required claim %s not found in refresh token query parameter", claimName)
      logger.warn(message)
      throw ApiException(
        message,
        400,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        "Invalid refresh token",
      )
    }
    return claim
  }

  private fun validateAndGetRefreshTokenPayloadClaims(refreshToken: String, clientId: UUID): Jws<Claims> {
    if (TokenGenerationAndValidation.validateJwtTokenSignature(refreshToken, secret)) {
      val claims = TokenGenerationAndValidation.parseClaims(refreshToken, secret)
      val exp = getClaim("exp", claims.body) as Int
      validateExpireTime(exp)
      val userIdInRefreshToken = getClaim("sub", claims.body) as String
      val clientIdInRefreshToken = getClaim("aud", claims.body) as String
      if (!userIdValidator.isValid(userIdInRefreshToken)) {
        val message = "Sub in refresh token do not match valid regex"
        logger.warn(message)
        throw ApiException(message, 400, ApiErrorTypes.INVALID_REQUEST.toString(), "invalid token")
      }
      if (clientId.toString() != clientIdInRefreshToken) {
        val message = String.format(
          "client id %s from auth header do not match with aud %s in refresh token",
          clientId,
          clientIdInRefreshToken,
        )
        logger.debug(message)
        throw ApiException(message, 400, ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid request")
      }
      return claims
    } else {
      val message = "Refresh token signature is invalid"
      logger.warn(message)
      throw ApiException(message, 400, ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid refresh token")
    }
  }

  private fun validateScopeInClaims(scopeInClaims: Any, scopeApprovedByUser: Set<Scope>) {
    val scopes = scopeInClaims as List<String>
    scopes.forEach { scope ->
      if (!Scope.isStringMatchEnumValue(scope, scopeApprovedByUser)) {
        throw ApiException("Permission denied", 403, ApiErrorTypes.ACCESS_DENIED.toString(), ACCESS_DENIED_MSG)
      }
    }
  }
}
