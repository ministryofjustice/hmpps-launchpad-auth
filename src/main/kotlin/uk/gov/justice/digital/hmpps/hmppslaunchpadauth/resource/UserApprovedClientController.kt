package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.BAD_REQUEST_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import java.util.*

@RestController
@RequestMapping("/v1")
class UserApprovedClientController(private var userApprovedClientService: UserApprovedClientService) {
  private val logger = LoggerFactory.getLogger(UserApprovedClientController::class.java)

  @GetMapping("/users/{user-id}/clients", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getUserApprovedClients(
    @PathVariable("user-id") userId: String,
    @RequestParam("page", required = false) page: Int?,
    @RequestParam("size", required = false) size: Int?,
  ): ResponseEntity<PagedResult<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserApprovedClientDto>> {
    validateUserIdFormat(userId)
    val pageNum = validatePage(page)
    val pageSize = validatePageSize(size)
    val userApprovedClients = userApprovedClientService
      .getUserApprovedClientsByUserId(userId, pageNum, pageSize)
    return ResponseEntity.status(HttpStatus.OK).body(userApprovedClients)
  }

  @DeleteMapping("/users/{user-id}/clients/{client-id}")
  fun revokeClientAccess(
    @PathVariable("user-id") userId: String,
    @PathVariable("client-id") clientId: UUID,
  ): ResponseEntity<Void> {
    validateUserIdFormat(userId)
    userApprovedClientService.revokeClientAccess(userId, clientId)
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
  }

  private fun validateUserIdFormat(userId: String) {
    val regex = "^[A-Z][0-9]{4}[A-Z]{2}\$".toRegex()
    if (!regex.matches(userId)) {
      logger.warn("Invalid user id format for user id", userId)
      throw ApiException("Invalid user id format for user id", BAD_REQUEST_CODE)
    }
  }

  private fun validatePage(page: Int?): Int {
    if (page == null) {
      return 1
    }
    if (page < 1) {
      throw ApiException("page cannot be less than 1", BAD_REQUEST_CODE)
    }
    return page
  }

  private fun validatePageSize(size: Int?): Int {
    if (size == null) {
      return 20
    }
    return size
  }
}
