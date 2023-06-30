package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ACCESS_DENIED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ACCESS_DENIED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoLoginService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoRequestService
import java.util.*

@RestController
@RequestMapping("/v1/oauth2")
class AuthController(private var clientService: ClientService,
  private var ssoRequestService: SsoRequestService,
  private var ssoLoginService: SsoLoginService,
  ) {
  @RequestMapping("/authorize")
  fun authorize(
    @RequestParam("client_id", required = true) clientId: UUID,
    @RequestParam("response_type", required = true) responseType: String,
    @RequestParam("scope", required = true) scope: String,
    @RequestParam("redirect_uri", required = true) redirectUri: String,
    @RequestParam("state", required = false) state: String?,
    @RequestParam("nonce", required = false) nonce: String?,
  ): RedirectView {
    val url = ssoLoginService.initiateSsoLogin(clientId, responseType, scope, redirectUri, state, nonce)
    println(url)
    return RedirectView(url)
  }

  @PostMapping("/callback", produces = ["application/json"], consumes = ["application/x-www-form-urlencoded"])
  fun getUserAuthCode(
    @RequestParam("id_token", required = false) token: String?,
    @RequestParam("state", required = true) state: UUID,
    @RequestParam("userApproval", required = false) userApproval: String?,
  ): Any {
      val client  = ssoRequestService.getClient(state)
      // Callback and it requires user approval
      if (!client.autoApprove && token != null) {
        ssoLoginService.generateAndUpdateSsoRequestWithAuthorizationCode(token, state, client.autoApprove)
        val modelAndView = ModelAndView("user_approval")
        modelAndView.addObject("token", token)
        modelAndView.addObject("state", state)
        modelAndView.addObject("logo", client.logoUri)
        modelAndView.addObject("name", "${client.name} would like to:")
        modelAndView.addObject("scopes", Scope.getTemplateTextByEnums(ssoRequestService.getSsoRequestScopes(state)).sorted())
        modelAndView.addObject("description", client.description)
        return modelAndView
        // Callback and it requires user approval and user approved
      } else if (!client.autoApprove && token == null && userApproval == "approved") {
        val url = ssoLoginService.generateAndUpdateSsoRequestWithAuthorizationCode(null, state, client.autoApprove)
        return RedirectView(url)
        // Callback and it requires user approval and user not approved
      } else if (!client.autoApprove && token == null && userApproval == "cancelled") {
        ssoLoginService.cancelAccess(state)
        throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
      } else {
        // Callback and user approval is not required
        val url = ssoLoginService.generateAndUpdateSsoRequestWithAuthorizationCode(token, state, client.autoApprove)
        return RedirectView(url)
      }

  }
}
