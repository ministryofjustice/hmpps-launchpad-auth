package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.JwkService

@RestController
@RequestMapping("/v1")
class JwkController(
  private var jwkService: JwkService,
) {
  @GetMapping("/.well-known/jwks.json")
  fun getPublicKey(): MutableMap<String?, Any?> {
    val jwk = jwkService.getJwk()
    return jwk
  }
}
