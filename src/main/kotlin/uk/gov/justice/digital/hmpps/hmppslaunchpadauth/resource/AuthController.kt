package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.ACCESS_DENIED_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_REQUEST_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.REDIRECTION_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoLogInService
import java.util.*

@RestController
@RequestMapping("/v1/oauth2")
class AuthController(private var ssoLoginService: SsoLogInService) {
  companion object {
    const val MAX_STATE_OR_NONCE_SIZE = 128
    const val SSO_SUPPORTED_RESPONSE_TYPE = "code"
  }

  @GetMapping("/authorize")
  fun authorize(
    @RequestParam("client_id") clientId: UUID,
    @RequestParam("response_type") responseType: String,
    @RequestParam scope: String,
    @RequestParam("redirect_uri") redirectUri: String,
    @RequestParam(required = false) state: String?,
    @RequestParam(required = false) nonce: String?,
  ): RedirectView {
    validateResponseType(responseType, redirectUri)
    validateSize(state, "state", redirectUri)
    validateSize(nonce, "nonce", redirectUri)
    val url = ssoLoginService.initiateSsoLogin(clientId, responseType, scope, redirectUri, state, nonce)
    return RedirectView(url)
  }

  @PostMapping("/callback", consumes = ["application/x-www-form-urlencoded"])
  fun getAuthCode(
    @RequestParam("id_token", required = false) token: String?,
    @RequestParam(required = true) state: UUID,
  ): Any {
    return ssoLoginService.updateSsoRequest(token, state)
  }

  @PostMapping("/authorize-client", consumes = ["application/x-www-form-urlencoded"])
  fun authorizeClient(
    @RequestParam state: UUID,
    @RequestParam userApproval: String,
    @RequestParam redirectUri: String,
  ): Any {
    if (userApproval == "approved") {
      return ssoLoginService.updateSsoRequest(null, state)
    } else {
      // user did not approved the client so delete sso request
      ssoLoginService.cancelAccess(state)
      throw SsoException(ACCESS_DENIED_MSG, REDIRECTION_CODE, ApiErrorTypes.ACCESS_DENIED.toString(), ACCESS_DENIED_MSG, redirectUri)
    }
  }

  private fun validateSize(value: String?, paramName: String, redirectUri: String) {
    // validate query param length, optional param can be null or if not null should not exceed 128 max size
    if (value != null) {
      if (value.length > MAX_STATE_OR_NONCE_SIZE) {
        val message = String.format("%s size exceeds 128 char size limit", paramName)
        throw SsoException(message, REDIRECTION_CODE, ApiErrorTypes.INVALID_REQUEST.toString(), INVALID_REQUEST_MSG, redirectUri)
      }
    }
  }

  private fun validateResponseType(responseType: String, redirectUri: String) {
    if (responseType != SSO_SUPPORTED_RESPONSE_TYPE) {
      val message = String.format("Response type: %s is not supported", responseType)
      throw SsoException(message, REDIRECTION_CODE, ApiErrorTypes.INVALID_REQUEST.toString(), INVALID_REQUEST_MSG, redirectUri)
    }
  }
}
