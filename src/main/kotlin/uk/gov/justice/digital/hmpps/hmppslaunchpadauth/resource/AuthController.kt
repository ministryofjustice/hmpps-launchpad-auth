package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_REQUEST_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoLogInService
import java.util.*

@RestController
@RequestMapping("/v1/oauth2")
class AuthController(private var ssoLoginService: SsoLogInService) {
  companion object {
    const val MAX_STATE_OR_NONCE_SIZE = 128
    const val SSO_SUPPORTED_RESPONSE_TYPE = "code"
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
  }

  @Value("\${launchpad.auth.allowlisted-scopes}")
  private lateinit var allowListedScopes: String

  @Tag(name = "initiate sign in", description = "Initiate sign in process to get code")
  @GetMapping("/authorize")
  fun authorize(
    @RequestParam("client_id") clientId: UUID,
    @RequestParam("response_type") responseType: String,
    @RequestParam scope: String,
    @RequestParam("redirect_uri") redirectUri: String,
    @RequestParam(required = false) state: String?,
    @RequestParam(required = false) nonce: String?,
  ): RedirectView {
    validateResponseType(responseType, redirectUri, state)
    validateSize(state, "state", redirectUri, state)
    validateSize(nonce, "nonce", redirectUri, state)
    val scopes = Scope.removeAllowListScopesNotRequired(scope, allowListedScopes.split(","))
    val url = ssoLoginService.initiateSsoLogin(clientId, responseType, scopes, redirectUri, state, nonce)
    logger.info("Sign in request sent to azure for client $clientId")
    return RedirectView(url)
  }

  @Tag(name = "get auth code", description = "Initiate sign in process to get code")
  @PostMapping("/callback", consumes = ["application/x-www-form-urlencoded"])
  fun getAuthCode(
    @RequestParam("id_token", required = false) token: String?,
    @RequestParam state: UUID,
  ): Any {
    return ssoLoginService.updateSsoRequest(token, state)
  }

  @PostMapping("/authorize-client", consumes = ["application/x-www-form-urlencoded"])
  fun authorizeClient(
    @RequestParam state: UUID,
    @RequestParam("user_approval") userApproval: String,
  ): Any {
    if (userApproval == "approved") {
      return ssoLoginService.updateSsoRequest(null, state)
    } else {
      // user did not approved the client so delete sso request
      return ssoLoginService.cancelAccess(state)
    }
  }

  private fun validateSize(value: String?, paramName: String, redirectUri: String, clientState: String?) {
    // validate query param length, optional param can be null or if not null should not exceed 128 max size
    if (value != null) {
      if (value.length > MAX_STATE_OR_NONCE_SIZE) {
        val message = "$paramName size exceeds 128 char size limit"
        throw SsoException(
          message,
          HttpStatus.FOUND,
          ApiErrorTypes.INVALID_REQUEST.toString(),
          INVALID_REQUEST_MSG,
          redirectUri,
          clientState,
        )
      }
    }
  }

  private fun validateResponseType(responseType: String, redirectUri: String, clientState: String?) {
    if (responseType != SSO_SUPPORTED_RESPONSE_TYPE) {
      val message = "Response type: $responseType is not supported"
      throw SsoException(
        message,
        HttpStatus.FOUND,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        INVALID_REQUEST_MSG,
        redirectUri,
        clientState,
      )
    }
  }
}
