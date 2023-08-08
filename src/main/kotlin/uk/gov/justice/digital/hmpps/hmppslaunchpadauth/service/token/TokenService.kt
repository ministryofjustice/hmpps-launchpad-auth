package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

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
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ACCESS_DENIED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ACCESS_DENIED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.BAD_REQUEST_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoRequestService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.PrisonerApiService
import java.net.URI
import java.util.*

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
      .orElseThrow { ApiException(String.format("Client with %s not found", clientId), BAD_REQUEST_CODE) }
    if (code != null && grantType == "code" && redirectUri != null) {
      validateGrant(grantType, clientId, client)
      val ssoRequest = ssoRequestService.getSsoRequestByAuthorizationCode(code).orElseThrow {
        throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
      }
      validateRedirectUri(redirectUri, ssoRequest)
      val token = generateToken(ssoRequest.userId!!, clientId, ssoRequest.client.scopes, ssoRequest.client.nonce)
      ssoRequestService.deleteSsoRequestById(ssoRequest.id)
      return token
    } else if (refreshToken != null && grantType == "refresh_token") {
      validateGrant(grantType, clientId, client)
      TokenGenerationAndValidation.validateJwtTokenSignature(refreshToken, secret)
      val claims = TokenGenerationAndValidation.parseClaims(refreshToken, secret)
      val userId = claims.body["sub"] as String
      val userApprovedClient =
        userApprovedClientService.getUserApprovedClientByUserIdAndClientId(userId, clientId).orElseThrow {
          throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
        }
      return generateToken(userId, clientId, userApprovedClient.scopes, nonce)
    } else {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun generateToken(prisonerId: String, clientId: UUID, scopes: Set<Scope>, nonce: String?): Token {
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

    val refreshTokenPayload = refreshToken.generatePayload(
      null,
      null,
      prisonerData.profile,
      clientId,
      scopes,
      nonce,
    )
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
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
    if (client.id != clientId) {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun validateRedirectUri(redirectUri: URI, ssoRequest: SsoRequest) {
    if (ssoRequest.client.redirectUri != redirectUri.toString()) {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }
}
