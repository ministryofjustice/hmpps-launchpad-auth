package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import java.util.*

@Service
class SsoLoginService(
  private var clientService: ClientService,
  private var ssoRequestService: SsoRequestService,
  private var tokenProcessor: TokenProcessor,
) {
  @Value("\${azure.oauth2-url}")
  private lateinit var azureOauthUrl: String

  @Value("\${azure.client-id}")
  private lateinit var launchpadClientId: String

  @Value("\${azure.launchpad-redirectUri}")
  private lateinit var launchpadRedirectUrl: String

  @Value("\${azure.issuer-url}")
  private lateinit var issuerUrl: String

  fun initiateSsoLogin(
    clientId: UUID,
    responseType: String,
    scopes: String,
    redirectUri: String,
    state: String?,
    nonce: String?,
  ): String {
    clientService.validateParams(clientId, responseType, scopes, redirectUri, state, nonce)
    val scopeSet = cleanScopes(scopes)
    val ssoRequestId = ssoRequestService.generateSsoRequest(
      Scope.getEnumsByValues(scopeSet),
      nonce,
      state,
      redirectUri,
      clientId,
    )
    return "$azureOauthUrl?response_type=id_token&client_id=$launchpadClientId&scope=openid email profile&state=$ssoRequestId&response_mode=form_post&redirect_uri=$launchpadRedirectUrl&nonce=$nonce"
  }

  fun generateAndUpdateSsoRequestWithAuthorizationCode(token: String, state: UUID): String {
    val userId = tokenProcessor.getUserId(token)
    return ssoRequestService.updateSsoRequestAuthCodeAndUserId(userId, state)
  }

  private fun cleanScopes(scopes: String): Set<String> {
    var scopeList: List<String>
    if (scopes.contains(" ")) {
      val scopeValues = scopes.replace("\\s+".toRegex(), " ")
      scopeList = scopeValues.split("\\s".toRegex())
      return HashSet(scopeList)
    } else {
      return setOf(scopes)
    }
  }
}