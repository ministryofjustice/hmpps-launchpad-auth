package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserClients
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import java.util.*

@RestController
@RequestMapping("/v1/")
class UserApprovedClientController(private var userApprovedClientService: UserApprovedClientService, private var clientService: ClientService) {
  @GetMapping("/users/{user-id}/clients")
  fun getUserApprovedClients(
    @PathVariable("user-id") userId: String,
    @RequestParam("page") page: Long,
    @RequestParam("size") size: Long,
    ): ResponseEntity<UserClients> {
    val userApprovedClients = userApprovedClientService.getUserApprovedClientsByUserId(userId, page, size)
    return ResponseEntity.status(HttpStatus.OK).body(userApprovedClients)
  }

  @DeleteMapping("/users/{user-id}/clients/{client-id}")
  fun revokeClientAccess(@PathVariable("user-id") userId: String,@PathVariable("client-id") clientId: UUID): ResponseEntity<Void> {
    userApprovedClientService.revokeClientAccess(userId, clientId)
    return ResponseEntity.status(HttpStatus.OK).build()
  }

}