package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SsoRequestService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService

@RestController
@RequestMapping("/v1/admin")
class AuthJobsEndpoints(
  private var userApprovedClientService: UserApprovedClientService,
  private var ssoRequestService: SsoRequestService,
) {
  @Hidden
  @PostMapping("/purge-stale-sso-tokens")
  fun deleteOldSsoRequest(): ResponseEntity<Void> {
    ssoRequestService.deleteOldSsoRequests()
    return ResponseEntity.status(HttpStatus.ACCEPTED).build()
  }

  @Hidden
  @PostMapping("/purge-inactive-users")
  fun deleteInactiveUserApproveClient(): ResponseEntity<Void> {
    userApprovedClientService.deleteInActiveUserApprovedClient()
    return ResponseEntity.status(HttpStatus.ACCEPTED).build()
  }
}
