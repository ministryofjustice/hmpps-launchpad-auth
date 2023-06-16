package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import java.util.*

@RestController
@RequestMapping("/v1/oauth2")
class AuthController(var clientService: ClientService) {

  @Value("\${azure.baseurl}")
  lateinit var azureBaseUrl: String

  @Value("\${azure.client-id}")
  lateinit var launchpadClientId: String

  @RequestMapping("/authorize")
  fun authorize(
    @RequestParam("client_id", required = true) clientId: UUID,
    @RequestParam("response_type", required = true) responseType: String,
    @RequestParam("scope", required = true) scope: String,
    @RequestParam("redirect_uri", required = true) redirectUri: String,
    @RequestParam("state", required = false) state: String?,
    @RequestParam("nonce", required = false) nonce: String?,
  ): RedirectView {
    clientService.validateParams(clientId, responseType, scope, redirectUri, state, nonce)
    // TO DO replace state and nonce according to design doc in later sprint ticket works
    return RedirectView("$azureBaseUrl?response_type=id_token&client_id=$launchpadClientId&scope=openid&state=$state&response_mode=form_post&redirect_uri=$redirectUri&nonce=$nonce")
  }
}
