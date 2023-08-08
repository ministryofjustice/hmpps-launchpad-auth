package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoRequestService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.UNAUTHORIZED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.UNAUTHORIZED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.PrisonerApiService
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.LinkedHashMap

const val TOKEN_TYPE = "Bearer"

@Component
class TokenService(
  private var prisonerApiService: PrisonerApiService,
  private var idTokenService: IdTokenPayload,
  private var accessTokenService: AccessTokenPayload,
  private var refreshToken: RefreshTokenPayload,
  private var clientService: ClientService,
  private var ssoRequestService: SsoRequestService,
  private var userApprovedClientService: UserApprovedClientService,
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
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
    if (code != null && grantType == "code" && redirectUri != null) {
      validateGrant(grantType, clientId, client)
      val ssoRequest = ssoRequestService.getSsoRequestByAuthorizationCode(code).orElseThrow {
        logger.warn("Sso Request with code {} not found", code)
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
      validateRedirectUri(redirectUri, ssoRequest)
      val token = generateToken(ssoRequest.userId!!, clientId, ssoRequest.client.scopes, ssoRequest.client.nonce, null)
      ssoRequestService.deleteSsoRequestById(ssoRequest.id)
      return token
    } else if (refreshToken != null && grantType == "refresh_token") {
      if (TokenGenerationAndValidation.validateJwtTokenSignature(refreshToken, secret)) {
        val claims = TokenGenerationAndValidation.parseClaims(refreshToken, secret)
        val exp = claims.body["exp"] as Int
        val userId = claims.body["sub"] as String
        val clientIdInString = claims.body["aud"] as String
        val clientId = UUID.fromString(clientIdInString)
        validateGrant(grantType, clientId, client)
        val userApprovedClient =
          userApprovedClientService.getUserApprovedClientByUserIdAndClientId(userId, clientId).orElseThrow {
            logger.warn("User approved client  with user id {} and client id {} not found", userId, clientId)
            throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
          }
        // add here logic for token not expired
        if (exp > LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {
          return generateToken(userId, clientId, userApprovedClient.scopes, nonce, claims)
        } else {
          return generateToken(userId, clientId, userApprovedClient.scopes, nonce, null)
        }
      } else {
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
    } else {
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }

  private fun generateToken(prisonerId: String, clientId: UUID, scopes: Set<Scope>, nonce: String?, refreshTokenPayloadOld: Jws<Claims>?): Token {
    val prisonerData = prisonerApiService.getPrisonerData(prisonerId)
    val idTokenPayload = idTokenService.generatePayload(
      prisonerData.booking,
      prisonerData.establishment,
      prisonerData.profile,
      clientId,
      scopes,
      nonce,
    )
    val accessTokenPayload = accessTokenService.generatePayload(
      null,
      null,
      prisonerData.profile,
      clientId,
      scopes,
      nonce,
    )
    var refreshTokenPayload = LinkedHashMap<String, Any>()
    if (refreshTokenPayloadOld != null) {
      val claims = refreshTokenPayloadOld.body
      claims.keys.forEach{
        value ->
        val v = value as String
        refreshTokenPayload[v] = claims[value] as Any
      }
    } else {
      refreshTokenPayload = refreshToken.generatePayload(
        null,
        null,
        prisonerData.profile,
        clientId,
        scopes,
        nonce,
      )
    }
    val idToken = TokenGenerationAndValidation
      .createToken(
        idTokenPayload,
        idTokenService.buildHeaderClaims(SignatureAlgorithm.HS256.name, "JWT"),
        SignatureAlgorithm.HS256,
        secret,
      )
    val accessToken = TokenGenerationAndValidation
      .createToken(
        accessTokenPayload,
        accessTokenService.buildHeaderClaims(SignatureAlgorithm.HS256.toString(), "JWT"),
        SignatureAlgorithm.HS256,
        secret,
      )
    refreshTokenPayload["ati"] = accessTokenPayload["jti"] as String
    val refreshToken = TokenGenerationAndValidation
      .createToken(
        refreshTokenPayload,
        accessTokenService.buildHeaderClaims(SignatureAlgorithm.HS256.toString(), "JWT"),
        SignatureAlgorithm.HS256,
        secret,
      )
    return Token(idToken, accessToken, refreshToken, TOKEN_TYPE, 3600L)
  }

  private fun validateGrant(grantType: String, clientId: UUID, client: Client) {
    val grant = AuthorizationGrantType.getAuthorizationGrantTypeByStringValue(grantType)
    if (!client.authorizedGrantTypes.contains(grant)) {
      logger.warn("Invalid grant type {} sent in get token request", grantType)
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
    if (client.id != clientId) {
      logger.warn("Client id {} in token and query not matching", clientId)
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }

  private fun validateRedirectUri(redirectUri: URI, ssoRequest: SsoRequest) {
    if (ssoRequest.client.redirectUri != redirectUri.toString()) {
      logger.warn("Redirect uri sent in token request not in list {}", redirectUri)
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }
}
