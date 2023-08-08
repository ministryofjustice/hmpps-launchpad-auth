package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenGenerationAndValidation
import java.util.*

const val BEARER = "Bearer "

@Component("tokenAuthentication")
class TokenAuthentication(
  private var clientService: ClientService,
  private var userApprovedClientService: UserApprovedClientService,
) : Authentication {
  @Value("\${auth.service.secret}")
  private lateinit var secret: String

  private val logger = LoggerFactory.getLogger(TokenAuthentication::class.java)
  override fun authenticate(credential: String): AuthenticationInfo {
    if (credential.startsWith(BEARER)) {
      val token = credential.replace(BEARER, "")
      if (TokenGenerationAndValidation.validateJwtTokenSignature(token, secret)) {
        val clientId: String
        val userId: String
        val accessTokenId: String
        val scopes: Any
        try {
          val jwsClaims = TokenGenerationAndValidation.parseClaims(token, secret)
          val claims = jwsClaims.body
          val expireAt = claims["exp"] as Int
          TokenGenerationAndValidation.validateExpireTime(expireAt)
          accessTokenId = claims["jti"] as String
          // verify access token can be cast as UUID
          UUID.fromString(accessTokenId)
          clientId = claims["aud"] as String
          userId = claims["sub"] as String
          scopes = claims["scopes"] as Any
        } catch (e: Exception) {
          logger.debug("Exception during bearer token authentication: {}", e.message)
          throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
        }
        val client = clientService
          .getClientById(UUID.fromString(clientId))
          .orElseThrow {
            logger.warn("Client record do not exist for sub {} in token", clientId)
            throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
          }
        if (!client.enabled) {
          logger.warn("Client {} is not enabled", clientId)
          throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
        }
        val userApprovedClient = userApprovedClientService
          .getUserApprovedClientByUserIdAndClientId(userId, UUID.fromString(clientId)).orElseThrow {
            logger.warn("Client {} is not enabled", clientId)
            throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
          }
        validateScopeInClaims(scopes, userApprovedClient.scopes)
        return AuthenticationUserInfo(client.id, client.scopes, userId, userApprovedClient.scopes)
      } else {
        logger.error("Validation failed for bearer token")
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
    } else {
      logger.error("Token is not a valid bearer token {}", credential)
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }

  private fun validateScopeInClaims(scopeInClaims: Any, scopeApprovedByUser: Set<Scope>) {
    val scopes = scopeInClaims as List<String>
    scopes.forEach { scope ->
      if (!Scope.isStringMatchEnumValue(scope, scopeApprovedByUser)) {
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
    }
  }
}
