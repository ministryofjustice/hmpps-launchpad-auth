package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.ACCESS_DENIED_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_AZURE_AD_TENANT
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_CLIENT_ID_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_SCOPE_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_USER_ID
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import java.nio.charset.StandardCharsets
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
  @Value("\${azure.oauth2-base-url}")
  private lateinit var oauth2BaseUrl: String

  @Value("\${azure.tenant-id}")
  private lateinit var tenantId: String

  @Value("\${azure.oauth2-api-path}")
  private lateinit var oauth2ApiPath: String

  @Value("\${azure.client-id}")
  private lateinit var launchpadClientId: String

  @Value("\${azure.launchpad-redirectUri}")
  private lateinit var launchpadRedirectUrl: String

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
    logger.info(
      "Single sign on request received for client: {}, with response_type: {}, scopes: {}, redirect_uri: {}",
      clientId,
      responseType,
      scopes,
      redirectUri,
    )
    clientService.validateParams(clientId, responseType, scopes, redirectUri, state, nonce)
    val scopeSet = Scope.cleanScopes(scopes)
    val scopesEnums = getScopeEnumsFromValues(scopeSet)
    val ssoRequest = ssoRequestService.generateSsoRequest(
      scopesEnums,
      state,
      nonce,
      redirectUri,
      clientId,
    )
    return UriComponentsBuilder.fromHttpUrl(builtAzureOauth2Url())
      .queryParam("response_type", "id_token")
      .queryParam("client_id", launchpadClientId)
      .queryParam("scope", "openid email")
      .queryParam("state", ssoRequest.id)
      .queryParam("nonce", ssoRequest.nonce)
      .queryParam("response_mode", "form_post")
      .queryParam("redirect_uri", launchpadRedirectUrl)
      .build().toUriString()
  }

  fun updateSsoRequest(token: String?, state: UUID): Any {
    logger.info("jwt token received from azure")
    var ssoRequest = ssoRequestService.getSsoRequestById(state).orElseThrow {
      val message = "State $state send on callback url do not exist"
      ApiException(message, HttpStatus.FORBIDDEN, ApiErrorTypes.ACCESS_DENIED.toString(), ACCESS_DENIED_MSG)
    }
    val clientId = ssoRequest.client.id
    val client = clientService.getClientById(clientId).orElseThrow {
      val message = "Client $clientId of sso request do not exist"
      SsoException(
        message,
        HttpStatus.FOUND,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        INVALID_CLIENT_ID_MSG,
        ssoRequest.client.redirectUri,
        ssoRequest.client.state,
      )
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

  fun cancelAccess(state: UUID): RedirectView {
    val ssoRequest = ssoRequestService.getSsoRequestById(state)
      .orElseThrow {
        val message = "Sso request not found for state $state send when user not allowed the access in approval page"
        ApiException(message, HttpStatus.FORBIDDEN, ApiErrorTypes.ACCESS_DENIED.toString(), ACCESS_DENIED_MSG)
      }
    ssoRequestService.deleteSsoRequestById(state)
    logger.info("User {} did not approve the access for client {}", ssoRequest.userId, ssoRequest.client.id)
    val url = buildClientRedirectUrlAccessForNotApproved(ssoRequest)
    return RedirectView(url)
  }

  private fun buildClientRedirectUrl(ssoRequest: SsoRequest): String {
    return UriComponentsBuilder.fromHttpUrl(ssoRequest.client.redirectUri)
      .queryParam("code", ssoRequest.authorizationCode)
      .queryParamIfPresent("state", Optional.ofNullable(getEncodedValue(ssoRequest.client.state)))
      .build(true).toUriString()
  }

  private fun buildClientRedirectUrlAccessForNotApproved(ssoRequest: SsoRequest): String {
    return UriComponentsBuilder.fromHttpUrl(ssoRequest.client.redirectUri)
      .queryParam("error", ApiErrorTypes.ACCESS_DENIED.toString())
      .queryParam("error_description", ACCESS_DENIED_MSG)
      .queryParamIfPresent("state", Optional.ofNullable(getEncodedValue(ssoRequest.client.state)))
      .build(true).toUriString()
  }

  private fun getEncodedValue(value: String?): String? {
    var encodedValue: String? = null
    if (StringUtils.hasText(value)) {
      encodedValue = UriUtils.encode(value, StandardCharsets.UTF_8)
    }
    return encodedValue
  }

  private fun updateSsoRequestWithUserId(token: String, ssoRequest: SsoRequest): SsoRequest {
    try {
      val userId = tokenProcessor.getUserId(token, ssoRequest.nonce.toString())
      ssoRequest.userId = userId
      return ssoRequestService.updateSsoRequest(ssoRequest)
    } catch (e: IllegalArgumentException) {
      var errorType: String
      if (e.message == INVALID_AZURE_AD_TENANT || e.message == INVALID_USER_ID) {
        errorType = ApiErrorTypes.ACCESS_DENIED.toString()
      } else {
        errorType = ApiErrorTypes.SERVER_ERROR.toString()
      }
      throw SsoException(
        e.message!!,
        HttpStatus.FOUND,
        errorType,
        e.message!!,
        ssoRequest.client.redirectUri,
        ssoRequest.client.state,
      )
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
        if (!((userApprovedClient.scopes.containsAll(ssoRequest.client.scopes) && ssoRequest.client.scopes.containsAll(userApprovedClient.scopes)))) {
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
    return modelAndView
  }

  private fun getScopeEnumsFromValues(scopes: Set<String>): Set<Scope> {
    try {
      return Scope.getScopesByValues(scopes)
    } catch (e: IllegalArgumentException) {
      val message = "Invalid scope ${e.message}"
      throw ApiException(message, HttpStatus.BAD_REQUEST, ApiErrorTypes.INVALID_SCOPE.toString(), INVALID_SCOPE_MSG)
    }
  }

  private fun builtAzureOauth2Url(): String {
    return "$oauth2BaseUrl/$tenantId/$oauth2ApiPath"
  }
}
