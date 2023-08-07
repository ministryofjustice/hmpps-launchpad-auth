package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenGeneration
import java.util.*

const val BEARER = "Bearer "
@Component("tokenAuthentication")
class TokenAuthentication(
  private var clientService: ClientService,
  private var userApprovedClientService: UserApprovedClientService,
) : Authentication {
  @Value("\${auth.service.secret}")
  private lateinit var secret: String
  override fun authenticate(credential: String): AuthenticationInfo {
    if (credential != null && credential.startsWith(BEARER)) {
      val token = credential.replace(BEARER, "")
      if (TokenGeneration.validateJwtTokenSignature(token, secret)) {
        val claims = TokenGeneration.parseClaims(token, secret)
        val clientId = claims.body["aud"] as String
        val userId = claims.body["sub"] as String
        val client = clientService
          .getClientById(UUID.fromString(clientId))
          .orElseThrow { throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE) }
        if (!client.enabled) {
          throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
        }
        val userApprovedClient = userApprovedClientService
          .getUserApprovedClientByUserIdAndClientId(userId, UUID.fromString(clientId)).orElseThrow {
            throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
          }
        return AuthenticationUserInfo(client.id, client.scopes, userId, userApprovedClient.scopes)
      } else {
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
    } else {
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }

  //private fun validateScopeInClaims(scopeInClaims: Set<String>, scopeApprovedByUser: Set<Scope>): Boolean {
  //  TODO("Implement method")
  //}
}