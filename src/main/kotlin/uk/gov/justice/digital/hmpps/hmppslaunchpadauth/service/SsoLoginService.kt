package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import java.util.*

@Component
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

  private val logger = LoggerFactory.getLogger(SsoLoginService::class.java)

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
    val scopesEnums = Scope.getEnumsByValues(scopeSet)
    val ssoRequest = ssoRequestService.generateSsoRequest(
      scopesEnums,
      state,
      nonce,
      redirectUri,
      clientId,
    )
    return "$azureOauthUrl?response_type=id_token&client_id=$launchpadClientId&scope=openid email profile&state=${ssoRequest.id}&response_mode=form_post&redirect_uri=$launchpadRedirectUrl&nonce=${ssoRequest.nonce}"
  }

  fun generateAndUpdateSsoRequestWithAuthorizationCode(token: String?, state: UUID, autoApprove: Boolean): String {
    val ssoRequestFound = ssoRequestService.getSsoRequestById(state)
    if (ssoRequestFound.isPresent) {
      val ssoRequest = ssoRequestFound.get()
      // auto approved client
      if (token != null && ssoRequest.userId == null && ssoRequest.authorizationCode == null && autoApprove) {
        val userId = ssoRequest.nonce?.let { tokenProcessor.getUserId(token, it) }
        ssoRequest.userId = userId
        ssoRequest.authorizationCode = UUID.randomUUID()
        val updatedSsoRequest = ssoRequestService.updateSsoRequest(ssoRequest)
        return buildClientRedirectUrl(updatedSsoRequest)
        // Before User Approval
      } else if (token != null && ssoRequest.userId == null && ssoRequest.authorizationCode == null && !autoApprove) {
        val userId = ssoRequest.nonce?.let { tokenProcessor.getUserId(token, it) }
        ssoRequest.userId = userId
        ssoRequestService.updateSsoRequest(ssoRequest)
        val updatedSsoRequest = ssoRequestService.updateSsoRequest(ssoRequest)
        return buildClientRedirectUrl(updatedSsoRequest)
        // After user approval
      } else if (token == null && ssoRequest.userId != null && ssoRequest.authorizationCode == null) {
        ssoRequest.authorizationCode = UUID.randomUUID()
        ssoRequestService.updateSsoRequest(ssoRequest)
        val updatedSsoRequest = ssoRequestService.updateSsoRequest(ssoRequest)
        return buildClientRedirectUrl(updatedSsoRequest)
      } else {
        ssoRequestService.deleteSsoRequestById(ssoRequest.id)
        logger.warn(String.format("Form re-submittion ", ssoRequest.client.id))
        throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
      }
    } else {
      logger.warn(String.format("State send on callback url do not exist %s", state))
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  fun cancelAccess(state: UUID) {
    // user cancel approval in sso login user approval dialog
    ssoRequestService.deleteSsoRequestById(state)
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

  private fun buildClientRedirectUrl(ssoRequest: SsoRequest): String {
    return "${ssoRequest.client.reDirectUri}?code=${ssoRequest.authorizationCode}&state=${ssoRequest.client.state}"
  }
}
