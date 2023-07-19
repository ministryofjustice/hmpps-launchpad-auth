package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Component
class SsoLogInService(
  private var clientService: ClientService,
  private var ssoRequestService: SsoRequestService,
  private var tokenProcessor: TokenProcessor,
  private var userApprovedClientService: UserApprovedClientService,
) {
  @Value("\${azure.oauth2-url}")
  private lateinit var azureOauthUrl: String

  @Value("\${azure.client-id}")
  private lateinit var launchpadClientId: String

  @Value("\${azure.launchpad-redirectUri}")
  private lateinit var launchpadRedirectUrl: String

  @Value("\${azure.issuer-url}")
  private lateinit var issuerUrl: String

  private val logger = LoggerFactory.getLogger(SsoLogInService::class.java)

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
      .queryParam("scope", "openid")
      .queryParam("state", ssoRequest.id)
      .queryParam("nonce", ssoRequest.nonce)
      .queryParam("response_mode", "form_post")
      .queryParam("redirect_uri", launchpadRedirectUrl)
      .build().toUriString()
  }

  fun updateSsoRequestWithUserId(token: String?, state: UUID, autoApprove: Boolean): String {
    val ssoRequest = ssoRequestService.getSsoRequestById(state).orElseThrow {
      logger.warn(String.format("State send on callback url do not exist %s", state))
      ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
    if (token != null) {
      // auto approved client1
      val ssoRequest = updateSsoRequestWithUserId(token, ssoRequest)
      if (ssoRequest.userId != null && autoApprove) {
        createOrUpdateUserApprovedClient(ssoRequest)
      }

      return buildClientRedirectUrl(updateSsoRequestWithUserId(token, ssoRequest))
    } else {
      // Auto Approve = false and After user approval
      if (ssoRequest.userId != null) {
        createOrUpdateUserApprovedClient(ssoRequest)
      }
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
      .build().toUriString()
  }

  private fun updateSsoRequestWithUserId(token: String, ssoRequest: SsoRequest): SsoRequest {
    val userId = tokenProcessor.getUserId(token, ssoRequest.nonce.toString())
    ssoRequest.userId = userId
    return ssoRequestService.updateSsoRequest(ssoRequest)
  }

  private fun createOrUpdateUserApprovedClient(ssoRequest: SsoRequest) {
    ssoRequest.userId?.let {
      val userApprovedClientIfExist =   userApprovedClientService
        .getUserApprovedClientByUserIdAndClientId(
          it,
          ssoRequest.client.id,
          )
      if (userApprovedClientIfExist.isPresent) {
        val userApprovedClient = userApprovedClientIfExist.get()
        if (userApprovedClient.scopes != ssoRequest.client.scopes) {
          userApprovedClient.scopes = ssoRequest.client.scopes
          userApprovedClient.lastModifiedDate = LocalDateTime.now(ZoneOffset.UTC)
        } else {
          userApprovedClient.lastModifiedDate = LocalDateTime.now(ZoneOffset.UTC)
        }
        userApprovedClientService.updateUserApprovedClient(userApprovedClient)
      } else {
        val userApprovedClient = UserApprovedClient(
          UUID.randomUUID(),
          ssoRequest.userId!!,
          ssoRequest.client.id,
          ssoRequest.client.scopes,
          LocalDateTime.now(ZoneOffset.UTC),
          LocalDateTime.now(ZoneOffset.UTC),
        )
        userApprovedClientService.createUserApprovedClient(userApprovedClient)
      }
    }
  }
}
