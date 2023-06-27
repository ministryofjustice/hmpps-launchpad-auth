package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoRequestService
import java.net.URL
import java.util.*

@RestController
@RequestMapping("/v1/oauth2")
class AuthController(private var clientService: ClientService,
  private var ssoRequestService: SsoRequestService,
  ) {

  @Value("\${azure.oauth2-url}")
  private lateinit var azureOauthUrl: String

  @Value("\${azure.client-id}")
  private lateinit var launchpadClientId: String

  @RequestMapping("/authorize")
  fun authorize(
    @RequestParam("client_id", required = true) clientId: UUID,
    @RequestParam("response_type", required = true) responseType: String,
    @RequestParam("scope", required = true) scope: String,
    @RequestParam("redirect_uri", required = true) redirectUri: String,
    @RequestParam("state", required = false) state: String?,
    @RequestParam("nonce", required = false) nonce: String?,
  ): RedirectView {
    val state = clientService.validateParams(clientId, responseType, scope, redirectUri, state, nonce)
    // TO DO replace state and nonce according to design doc in later sprint ticket works
    val url = "http://localhost:8080/launchpad/logged/user/code"
    return RedirectView("$azureOauthUrl?response_type=id_token&client_id=$launchpadClientId&scope=openid email profile&state=$state&response_mode=form_post&redirect_uri=$url&nonce=$nonce")
  }

  // @CrossOrigin("http://localhost:8080")
  @PostMapping("/callback", produces = ["application/json"], consumes = ["application/x-www-form-urlencoded"])
  fun getUserAuthCode(
    @RequestParam("id_token", required = true) token: String,
    @RequestParam("state", required = true) state: UUID,
    @RequestParam("approve", required = false) approve: Boolean = false,
  ): Any {
    if (token != null) {
      println(token)

      val decoder: Base64.Decoder = Base64.getUrlDecoder()
      val chunks = token.split(".")
      // val idToken = IdToken()
      // idToken.header = String(decoder.decode(chunks[0]))
      // idToken.payload = String(decoder.decode(chunks[1]))
      // idToken.signature = String(decoder.decode(chunks[2]))
      val client  = ssoRequestService.getClient(state)
      if (!client.autoApprove && !approve) {
        val modelAndView = ModelAndView("userapproval")
        modelAndView.addObject("token", token)
        modelAndView.addObject("state", state)
        modelAndView.addObject("logo", client.logoUri)
        modelAndView.addObject("name", client.name)
        modelAndView.addObject("description", client.description)
        modelAndView.addObject("approve", true)
        return modelAndView
      } else {
        val url = ssoRequestService.updateSsoRequestAuthCodeAndUserId(token, state)
        return RedirectView(url)

      }
    } else {
      throw ApiException("null token", 400)
    }
  }
}
