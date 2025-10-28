package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.analytics.AppInsightEventType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.analytics.TelemetryService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.ACCESS_DENIED_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.EXPIRE_TOKEN_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_CODE_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_GRANT_TYPE_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_REDIRECT_URI_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_REQUEST_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_TOKEN_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SandboxSsoService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoRequestService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.PrisonerApiService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.UserClaims
import java.net.URI
import java.util.*

const val TOKEN_TYPE = "Bearer"

@Component
class TokenService(
  private var prisonerApiService: PrisonerApiService,
  private var clientService: ClientService,
  private var ssoRequestService: SsoRequestService,
  private var userApprovedClientService: UserApprovedClientService,
  private var telemetryService: TelemetryService,
  @Value("\${launchpad.auth.id-token-validity-seconds}")
  private var idTokenValiditySeconds: Long,
  @Value("\${launchpad.auth.access-token-validity-seconds}")
  private var accessTokenValiditySeconds: Long,
  @Value("\${launchpad.auth.refresh-token-validity-seconds}")
  private var refreshTokenValiditySeconds: Long,
) {

  @Value("\${launchpad.auth.private-key}")
  private lateinit var privateKey: String

  @Value("\${launchpad.auth.public-key}")
  private lateinit var publicKey: String

  @Value("\${launchpad.auth.kid}")
  private lateinit var kid: String

  @Value("\${launchpad.auth.iss-url}")
  private lateinit var issuerUrl: String

  @Value("\${launchpad.auth.sandbox-user}")
  private lateinit var sandboxUser: String

  companion object {
    private val logger = LoggerFactory.getLogger(TokenService::class.java)
  }

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
        val message = "Client with id $clientId not found"
        throw ApiException(
          message,
          HttpStatus.FORBIDDEN,
          ApiErrorTypes.ACCESS_DENIED.toString(),
          ACCESS_DENIED_MSG,
        )
      }
    if (code != null && grantType == AuthorizationGrantType.AUTHORIZATION_CODE.toString() && redirectUri != null) {
      return generateTokenByCode(code, grantType, redirectUri, client)
    } else if (refreshToken != null && grantType == AuthorizationGrantType.REFRESH_TOKEN.toString()) {
      return generateTokenByRefreshToken(refreshToken, grantType, client, nonce)
    } else {
      throw ApiException(
        INVALID_REQUEST_MSG,
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        INVALID_REQUEST_MSG,
      )
    }
  }

  private fun generateTokenByCode(code: UUID, grantType: String, redirectUri: URI, client: Client): Token {
    logger.info("Generating token for code for client id: ${client.id}")
    validateGrant(grantType, client.authorizedGrantTypes)
    val ssoRequest = ssoRequestService.getSsoRequestByAuthorizationCode(code)
      .orElseThrow {
        val message = "Sso Request with code $code not found"
        throw ApiException(
          message,
          HttpStatus.BAD_REQUEST,
          ApiErrorTypes.INVALID_CODE.toString(),
          INVALID_CODE_MSG,
        )
      }
    if (!ssoRequest.client.id.equals(client.id)) {
      val message = "Client id ${client.id} in token do not match with sso request client id ${ssoRequest.client.id}"
      throw ApiException(
        message,
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_CODE.toString(),
        INVALID_CODE_MSG,
      )
    }
    validateRedirectUri(redirectUri, ssoRequest)
    val token = generateToken(ssoRequest.userId!!, client.id, ssoRequest.client.scopes, ssoRequest.client.nonce, null, client.sandbox)
    ssoRequestService.deleteSsoRequestById(ssoRequest.id)
    return token
  }

  private fun generateTokenByRefreshToken(
    refreshToken: String,
    grantType: String,
    client: Client,
    nonce: String?,
  ): Token {
    logger.info("Generating token for refresh token for client id: ${client.id}")
    validateGrant(grantType, client.authorizedGrantTypes)
    val refreshTokenPayloadOld = validateAndGetRefreshTokenPayloadClaims(refreshToken, client.id)
    val userId = refreshTokenPayloadOld.payload["sub"] as String
    val scopes = refreshTokenPayloadOld.payload["scopes"] as Any
    val userApprovedClient =
      userApprovedClientService.getUserApprovedClientByUserIdAndClientId(userId, client.id)
        .orElseThrow {
          val message = "User approved client  with user id $userId and client id ${client.id} not found"
          throw ApiException(
            message,
            HttpStatus.FORBIDDEN,
            ApiErrorTypes.ACCESS_DENIED.toString(),
            ACCESS_DENIED_MSG,
          )
        }
    validateScopeInRefreshTokenClaims(scopes, userApprovedClient.scopes)
    return generateToken(userId, client.id, userApprovedClient.scopes, nonce, refreshTokenPayloadOld.payload, client.sandbox)
  }

  private fun generateToken(
    prisonerId: String,
    clientId: UUID,
    scopes: Set<Scope>,
    nonce: String?,
    refreshTokenPayloadOld: Claims?,
    sandbox: Boolean,
  ): Token {
    var prisonerData: UserClaims
    if (sandbox) {
      prisonerData = SandboxSsoService.getThirdPartyTestUser(sandboxUser, scopes)
    } else {
      prisonerData = prisonerApiService.getPrisonerData(prisonerId, clientId)
    }
    val idTokenPayload = IdTokenPayload()
    val idTokenPayloadClaims = idTokenPayload.generatePayload(
      prisonerData.booking,
      prisonerData.establishment,
      prisonerData.user,
      clientId,
      scopes,
      nonce,
      issuerUrl,
      idTokenValiditySeconds,
    )
    val accessTokenPayload = AccessTokenPayload()
    val accessTokenPayloadClaims = accessTokenPayload.generatePayload(
      prisonerData.user,
      clientId,
      scopes,
      issuerUrl,
      accessTokenValiditySeconds,
    )
    val accessTokenId = accessTokenPayloadClaims["jti"] as String
    var refreshTokenPayloadClaims = LinkedHashMap<String, Any>()
    if (refreshTokenPayloadOld != null) {
      accessTokenPayloadClaims["scopes"] = refreshTokenPayloadOld["scopes"] as Any
      refreshTokenPayloadOld.keys.forEach { value ->
        val v = value as String
        refreshTokenPayloadClaims[v] = refreshTokenPayloadOld[value] as Any
      }
      refreshTokenPayloadClaims["ati"] = accessTokenId
    } else {
      val refreshTokenPayload = RefreshTokenPayload()
      val accessTokenId = accessTokenPayloadClaims["jti"] as String
      refreshTokenPayloadClaims = refreshTokenPayload.generatePayload(
        accessTokenId,
        prisonerData.user,
        clientId,
        scopes,
        refreshTokenValiditySeconds,
      )
    }
    val idToken = TokenGenerationAndValidation
      .generateJwtToken(
        idTokenPayloadClaims,
        TokenCommonClaims.buildHeaderClaims(kid),
        privateKey,
      )
    val accessToken = TokenGenerationAndValidation
      .generateJwtToken(
        accessTokenPayloadClaims,
        TokenCommonClaims.buildHeaderClaims(kid),
        privateKey,
      )
    val refreshToken = TokenGenerationAndValidation
      .generateJwtToken(
        refreshTokenPayloadClaims,
        TokenCommonClaims.buildHeaderClaims(kid),
        privateKey,
      )
    val eventType =
      if (refreshTokenPayloadOld == null) AppInsightEventType.TOKEN_GENERATED_VIA_AUTHORIZATION_CODE else AppInsightEventType.TOKEN_GENERATED_VIA_REFRESH_TOKEN
    telemetryService.addTelemetryData(
      eventType,
      idTokenPayloadClaims,
    )
    return Token(idToken, accessToken, refreshToken, TOKEN_TYPE, accessTokenValiditySeconds - 1)
  }

  private fun validateGrant(grantType: String, authorizationGrantType: Set<AuthorizationGrantType>) {
    var grantEnum: AuthorizationGrantType
    try {
      grantEnum = AuthorizationGrantType.getAuthorizationGrantTypeByStringValue(grantType)
    } catch (e: IllegalArgumentException) {
      val message = "$grantType is invalid grant type"
      throw ApiException(
        message,
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_GRANT.toString(),
        INVALID_GRANT_TYPE_MSG,
      )
    }
    if (!authorizationGrantType.contains(grantEnum)) {
      val message = "Invalid grant type: $grantType sent in get token request"
      throw ApiException(
        message,
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_GRANT.toString(),
        INVALID_GRANT_TYPE_MSG,
      )
    }
  }

  private fun validateRedirectUri(redirectUri: URI, ssoRequest: SsoRequest) {
    if (ssoRequest.client.redirectUri != redirectUri.toString()) {
      val message = "Redirect uri: $redirectUri sent in token request not in list"
      throw ApiException(
        message,
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_REDIRECT_URI.toString(),
        INVALID_REDIRECT_URI_MSG,
      )
    }
  }

  private fun validateExpireTime(exp: Long) {
    try {
      TokenGenerationAndValidation.validateExpireTime(exp)
    } catch (e: IllegalArgumentException) {
      throw ApiException(
        "Expired refresh token",
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.EXPIRED_TOKEN.toString(),
        EXPIRE_TOKEN_MSG,
      )
    }
  }

  private fun getClaim(claimName: String, claims: Claims): Any {
    val claim = claims[claimName]
    if (claim == null) {
      val message = "Required claim $claimName not found in refresh token query parameter"
      throw ApiException(
        message,
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        "Invalid refresh token",
      )
    }
    return claim
  }

  private fun validateAndGetRefreshTokenPayloadClaims(refreshToken: String, clientId: UUID): Jws<Claims> {
    if (TokenGenerationAndValidation.validateJwtTokenSignature(refreshToken, publicKey)) {
      val claims = TokenGenerationAndValidation.parseClaims(refreshToken, publicKey)
      checkIfAccessToken(claims.payload)
      val exp = getClaim("exp", claims.payload) as Long
      val jti = getClaim("jti", claims.payload) as String
      validateAndGetUUIDInClaim(jti, "jti")
      getClaim("iat", claims.payload) as Long
      validateExpireTime(exp)
      getClaim("sub", claims.payload) as String
      val client = getClaim("aud", claims.payload) as LinkedHashSet<Any>
      val clientIdInRefreshToken = client.first
      if (clientId.toString() != clientIdInRefreshToken) {
        val message =
          "client id $clientId from auth header do not match with aud $clientIdInRefreshToken in refresh token"
        throw ApiException(
          message,
          HttpStatus.FORBIDDEN,
          ApiErrorTypes.ACCESS_DENIED.toString(),
          INVALID_REQUEST_MSG,
        )
      }
      return claims
    } else {
      val message = "Refresh token signature is invalid"
      throw ApiException(
        message,
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        ACCESS_DENIED_MSG,
      )
    }
  }

  private fun validateScopeInRefreshTokenClaims(scopeInClaims: Any, scopeApprovedByUser: Set<Scope>) {
    val scopes = scopeInClaims as List<String>
    scopes.forEach { scope ->
      if (!Scope.isStringMatchEnumValue(scope, scopeApprovedByUser)) {
        throw ApiException(
          "Scope in refresh token do not match with scopes approved by user",
          HttpStatus.BAD_REQUEST,
          ApiErrorTypes.INVALID_REQUEST.toString(),
          "Invalid refresh token",
        )
      }
    }
  }

  private fun checkIfAccessToken(claims: Claims) {
    val ati = claims["ati"]
    if (ati == null) {
      throw ApiException(
        "Access token sent as refresh token in query param",
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_TOKEN.toString(),
        INVALID_TOKEN_MSG,
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
}
