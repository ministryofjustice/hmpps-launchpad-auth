package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.JwkService

@RestController
@RequestMapping("/v1")
class JwkController(
  private var jwkService: JwkService,
) {
  @Operation(summary = "Well-known endpoints", description = "Supplies the JSON Web Key Set, containing public keys used to verify JSON Web Tokens.")
  @GetMapping("/.well-known/jwks.json")
  fun getPublicKey(): MutableMap<String?, Any?> {
    val jwk = jwkService.getJwk()
    return jwk
  }
}
