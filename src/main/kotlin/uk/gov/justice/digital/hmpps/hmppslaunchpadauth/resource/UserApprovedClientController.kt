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
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.BAD_REQUEST_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import java.util.*

@RestController
@RequestMapping("/v1/")
class UserApprovedClientController(private var userApprovedClientService: UserApprovedClientService, private var clientService: ClientService) {
  @GetMapping("/users/{user-id}/clients")
  fun getUserApprovedClients(
    @PathVariable("user-id") userId: String,
    @RequestParam("page") page: Int,
    @RequestParam("size") size: Int,
    ): ResponseEntity<UserClients> {
    if (userId.isEmpty()) {
      throw ApiException("User id is empty", BAD_REQUEST_CODE)
    }
    if (size > 20) {
      throw ApiException("Max allowed size of page is 20", BAD_REQUEST_CODE)
    }
    if (page < 1) {
      throw ApiException("Page number cannot be less than 1", BAD_REQUEST_CODE)
    }
    val userApprovedClients = userApprovedClientService.getUserApprovedClientsByUserId(userId, page, size)
    return ResponseEntity.status(HttpStatus.OK).body(userApprovedClients)
  }

  @DeleteMapping("/users/{user-id}/clients/{client-id}")
  fun revokeClientAccess(@PathVariable("user-id") userId: String,@PathVariable("client-id") clientId: UUID): ResponseEntity<Void> {
    userApprovedClientService.revokeClientAccess(userId, clientId)
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
  }

}