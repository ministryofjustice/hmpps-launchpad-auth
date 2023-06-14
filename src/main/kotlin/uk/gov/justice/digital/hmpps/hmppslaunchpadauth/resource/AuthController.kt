package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import java.util.*

const val AZURE_OAUTH2_BASE_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"

// T0 DO Get this value from environments properties
const val CLIENT_ID = "598471b7-0b6e-4922-a27b-6e4083046e98&"

@RestController
@RequestMapping("/v1/oauth2")
class AuthController(var clientService: ClientService) {
  @RequestMapping("/authorize")
  fun getUser(
    @RequestParam("client_id", required = true) clientId: UUID,
    @RequestParam("response_type", required = true) responseType: String,
    @RequestParam("scope", required = true) scope: String,
    @RequestParam("redirect_uri", required = true) redirectUri: String,
    @RequestParam("state", required = true) state: String,
    @RequestParam("nonce", required = true) nonce: String,
  ): RedirectView {
    clientService.validateParams(clientId, responseType, scope, redirectUri, state, nonce)
    return RedirectView("$AZURE_OAUTH2_BASE_URL?response_type=id_token&client_id=$CLIENT_ID&scope=openid&state=$state&response_mode=form_post&redirect_uri=$redirectUri&nonce=$nonce")
  }
}
