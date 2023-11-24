package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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

  @GetMapping("/purge-stale-sso-tokens")
  fun deleteOldSsoRequest(): ResponseEntity<Void> {
    ssoRequestService.deleteOldSsoRequests()
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
  }

  @GetMapping("/purge-inactive-users")
  fun deleteInactiveUserApproveClient(): ResponseEntity<Void> {
    userApprovedClientService.deleteInActiveUserApprovedClient()
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
  }
}
