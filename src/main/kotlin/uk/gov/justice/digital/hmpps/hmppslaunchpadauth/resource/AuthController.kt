package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ACCESS_DENIED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ACCESS_DENIED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.BAD_REQUEST_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoLogInService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoRequestService
import java.util.*

const val MAX_STATE_OR_NONCE_SIZE = 128
const val SSO_SUPPORTED_RESPONSE_TYPE = "code"

@RestController
@RequestMapping("/v1/oauth2")
class AuthController(private var clientService: ClientService,
                     private var ssoRequestService: SsoRequestService,
                     private var ssoLoginService: SsoLogInService,) {
  @GetMapping("/authorize")
  fun authorize(
    @RequestParam("client_id", required = true) clientId: UUID,
    @RequestParam("response_type", required = true) responseType: String,
    @RequestParam("scope", required = true) scope: String,
    @RequestParam("redirect_uri", required = true) redirectUri: String,
    @RequestParam("state", required = false) state: String?,
    @RequestParam("nonce", required = false) nonce: String?,
  ): RedirectView {
    validateResponseType(responseType)
    validateSize(state, "state")
    validateSize(nonce, "nonce")
    val url = ssoLoginService.initiateSsoLogin(clientId, responseType, scope, redirectUri, state, nonce)
    return RedirectView(url)
  }

  /*@PostMapping("/callback", consumes = ["application/x-www-form-urlencoded"])
  fun getAuthCode(
    @RequestParam("id_token", required = false) token: String?,
    @RequestParam("state", required = true) state: UUID,
    ): Any {
    val ssoRequest = ssoRequestService.getSsoRequestById(state).orElseThrow {ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)}
    val client  = clientService.getClientById(ssoRequest.client.id)
      .orElseThrow { ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE) }
    // Callback and it requires user approval for client
    if (!client.autoApprove && token != null) {
      val url = ssoLoginService.updateSsoRequestWithUserId(token, state, client.autoApprove)
      val modelAndView = ModelAndView("user_approval")
      if (url.isEmpty) {
        modelAndView.addObject("state", state)
        modelAndView.addObject("scopes", Scope.getTemplateTextByScopes(ssoRequest.client.scopes).sortedDescending())
        modelAndView.addObject("client", client)
        return modelAndView
      } else {
        return RedirectView(url.get())
      }
    } else {
      // Callback and user approval for client is not required
      val url = ssoLoginService.updateSsoRequestWithUserId(token, state, client.autoApprove)
      return RedirectView(url.get())
    }
  }*/

  @PostMapping("/callback", consumes = ["application/x-www-form-urlencoded"])
  fun getAuthCode(
    @RequestParam("id_token", required = false) token: String?,
    @RequestParam("state", required = true) state: UUID,
  ): Any {
    return ssoLoginService.updateSsoRequestWithUserId(token, state)
  }

  @PostMapping("/authorize-client", consumes = ["application/x-www-form-urlencoded"])
  fun authorizeClient(
    @RequestParam("state", required = true) state: UUID,
    @RequestParam("userApproval", required = true) userApproval: String?,
  ): Any {
    if (userApproval == "approved") {
      return ssoLoginService.updateSsoRequestWithUserId(null, state)
    } else {
      // user did not approved the client so delete sso request
      ssoLoginService.cancelAccess(state)
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun validateSize(value: String?, paramName: String) {
    // validate query param length, optional param can be null or if not null should not exceed 128 max size
    if (value != null) {
      if (value.length > MAX_STATE_OR_NONCE_SIZE) {
        throw ApiException(String.format("%s size exceeds 128 char size limit", paramName), BAD_REQUEST_CODE)
      }
    }
  }

  private fun validateResponseType(responseType: String) {
    if (responseType != SSO_SUPPORTED_RESPONSE_TYPE) {
      throw ApiException(String.format("Response type: %s is not supported", responseType), BAD_REQUEST_CODE)
    }
  }
}
