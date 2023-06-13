package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import java.util.UUID

@RestController
@RequestMapping("/v1/oauth2")
class AuthController(@Autowired var clientService: ClientService) {
  @RequestMapping("/authorize")
  fun getUser(
    @RequestParam("client_id") clientId: UUID,
    @RequestParam("response_type") responseType: String,
    @RequestParam("scope") scope: String,
    @RequestParam("redirect_uri") redirectUri: String,
    @RequestParam("state") state: String,
    @RequestParam("nonce") nonce: String,
  ): RedirectView {
    clientService.validateParams(clientId, responseType, scope, redirectUri, state, nonce)
    return RedirectView("https://login.microsoftonline.com/common/oauth2/v2.0/authorize?response_type=id_token&client_id=598471b7-0b6e-4922-a27b-6e4083046e98&scope=openid&state=$state&response_mode=form_post&redirect_uri=http://localhost:8080/launchpad/logged/user&nonce=$nonce")
  }
}
