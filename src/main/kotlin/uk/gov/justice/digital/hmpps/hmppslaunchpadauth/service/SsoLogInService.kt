package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.REDIRECTION_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
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

  companion object {
    private val logger = LoggerFactory.getLogger(SsoLogInService::class.java)
  }

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
    logger.info(
      "Single sign on request received for client: {}, with response_type: {}, scopes: {}, redirect_uri: {}",
      clientId,
      responseType,
      scopes,
      redirectUri
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

  fun updateSsoRequest(token: String?, state: UUID): Any {
    var ssoRequest = ssoRequestService.getSsoRequestById(state).orElseThrow {
      val message = String.format("State send on callback url do not exist %s", state)
      ApiException(message, HttpStatus.FORBIDDEN.value(), ApiErrorTypes.ACCESS_DENIED.toString(), "Permission not granted")
    }
    val clientId = ssoRequest.client.id
    val client = clientService.getClientById(clientId).orElseThrow {
      val message = String.format("Client of sso request  do not exist %s", clientId)
      SsoException(message, REDIRECTION_CODE, ApiErrorTypes.INVALID_CLIENT.toString(), "Client not found", ssoRequest.client.redirectUri)
    }
    var approvalRequired = false
    if (token != null) {
      ssoRequest = updateSsoRequestWithUserId(token, ssoRequest)
      if (client.autoApprove) {
        createOrUpdateUserApprovedClient(ssoRequest, true)
        logger.info("Successful sso login for client {} and user id {}", ssoRequest.client.id, ssoRequest.userId)
        return RedirectView(buildClientRedirectUrl(ssoRequest))
      } else {
        // Auto Approve = false and user approval is required
        approvalRequired = createOrUpdateUserApprovedClient(ssoRequest, false)
      }
    } else {
      if (!client.autoApprove) {
        // Auto Approve = false and user has already approved.
        if (ssoRequest.userId != null) {
          approvalRequired = createOrUpdateUserApprovedClient(ssoRequest, true)
        }
      }
    }
    if (approvalRequired) {
      return buildModelAndView(state, ssoRequest, client)
    } else {
      logger.info("Successful sso login for client {} and user id {}", ssoRequest.client.id, ssoRequest.userId)
      return RedirectView(buildClientRedirectUrl(ssoRequest))
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
    try {
      val userId = tokenProcessor.getUserId(token, ssoRequest.nonce.toString())
      ssoRequest.userId = userId
      return ssoRequestService.updateSsoRequest(ssoRequest)
    } catch (e: IllegalArgumentException) {
      throw SsoException(e.message!!, REDIRECTION_CODE, ApiErrorTypes.SERVER_ERROR.toString(), "Exception in token processing", ssoRequest.client.redirectUri)
    }
  }

  private fun createOrUpdateUserApprovedClient(ssoRequest: SsoRequest, canSaveIfNotExist: Boolean): Boolean {
    var approvalRequired = false
    ssoRequest.userId?.let {
      val userApprovedClientIfExist = userApprovedClientService
        .getUserApprovedClientByUserIdAndClientId(
          it,
          ssoRequest.client.id,
        )
      if (userApprovedClientIfExist.isPresent) {
        val userApprovedClient = userApprovedClientIfExist.get()
        if (!((userApprovedClient.scopes.containsAll(ssoRequest.client.scopes) && ssoRequest.client.scopes.containsAll(userApprovedClient.scopes)))
        ) {
          // if record exist approval require only when scope varies
          approvalRequired = true
          userApprovedClient.scopes = ssoRequest.client.scopes
        }
        userApprovedClient.lastModifiedDate = LocalDateTime.now(ZoneOffset.UTC)
        userApprovedClientService.upsertUserApprovedClient(userApprovedClient)
      } else {
        // record do not exist approval required
        approvalRequired = true
        if (canSaveIfNotExist) {
          // user has already approved the client
          val localDateTimeInUTC = LocalDateTime.now(ZoneOffset.UTC)
          approvalRequired = false
          val userApprovedClient = UserApprovedClient(
            UUID.randomUUID(),
            ssoRequest.userId!!,
            ssoRequest.client.id,
            ssoRequest.client.scopes,
            localDateTimeInUTC,
            localDateTimeInUTC,
          )
          userApprovedClientService.upsertUserApprovedClient(userApprovedClient)
        } else {
        }
      }
    }
    return approvalRequired
  }

  private fun buildModelAndView(state: UUID, ssoRequest: SsoRequest, client: Client): ModelAndView {
    val modelAndView = ModelAndView("user_approval")
    modelAndView.addObject("state", state)
    modelAndView.addObject("scopes", Scope.getTemplateTextByScopes(ssoRequest.client.scopes).sortedDescending())
    modelAndView.addObject("client", client)
    modelAndView.addObject("redirectUri", ssoRequest.client.redirectUri)
    return modelAndView
  }
}
