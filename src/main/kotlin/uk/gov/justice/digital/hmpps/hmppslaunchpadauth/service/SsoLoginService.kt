package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
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
    val scopeSet = Scope.cleanScopes(scopes)
    val scopesEnums = Scope.getScopesByValues(scopeSet)
    val ssoRequest = ssoRequestService.generateSsoRequest(
      scopesEnums,
      state,
      nonce,
      redirectUri,
      clientId,
    )
    return UriComponentsBuilder.fromHttpUrl(azureOauthUrl)
      .queryParam("response_type", "id_token")
      .queryParam("client_id", launchpadClientId)
      // without adding profile in scope token do not contain oid
      .queryParam("scope", "openid profile")
      .queryParam("state", ssoRequest.id)
      .queryParam("nonce", ssoRequest.nonce)
      .queryParam("response_mode", "form_post")
      .queryParam("redirect_uri", launchpadRedirectUrl)
      .build().toString()
  }

  fun generateAndUpdateSsoRequestWithAuthorizationCode(token: String?, state: UUID, autoApprove: Boolean): String {
    val ssoRequest= ssoRequestService.getSsoRequestById(state).orElseThrow {
      logger.warn(String.format("State send on callback url do not exist %s", state))
      ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
    if (token != null) {
      // auto approved client
      return buildClientRedirectUrl(updateSsoRequestWithUserId(token, ssoRequest))
    } else {
      // Auto Approve = false and After user approval
      return buildClientRedirectUrl(ssoRequest)
    }
  }

  fun cancelAccess(state: UUID) {
    // user cancel approval in sso login user approval dialog
    ssoRequestService.deleteSsoRequestById(state)
  }

  private fun buildClientRedirectUrl(ssoRequest: SsoRequest): String {
    return UriComponentsBuilder.fromHttpUrl(ssoRequest.client.redirectUri)
      .queryParam("code", ssoRequest.authorizationCode)
      .queryParamIfPresent("state", Optional.ofNullable(ssoRequest.client.state))
      .build().toString()
  }

  private fun updateSsoRequestWithUserId(token: String, ssoRequest: SsoRequest): SsoRequest {
    val userId = ssoRequest.nonce?.let { tokenProcessor.getUserId(token, it) }
    ssoRequest.userId = userId
    return ssoRequestService.updateSsoRequest(ssoRequest)
  }
}
