package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserApprovedClientDto
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ACCESS_DENIED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ACCESS_DENIED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.BAD_REQUEST_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.Authentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.util.*

@RestController
@RequestMapping("/v1")
class UserApprovedClientController(
  @Qualifier("tokenAuthentication") private var authentication: Authentication,
  private var userApprovedClientService: UserApprovedClientService,
  private var userIdValidator: UserIdValidator
) {

  @GetMapping("/users/{user-id}/clients", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getUserApprovedClients(
    @PathVariable("user-id") userId: String,
    @RequestParam("page", required = false) page: Int?,
    @RequestParam("size", required = false) size: Int?,
    @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
  ): ResponseEntity<PagedResult<UserApprovedClientDto>> {
    val authenticationInfo = authentication.authenticate(authorization)
    validateScope(Scope.USER_CLIENTS_READ, authenticationInfo.clientScope)
    validateUserId(userId)
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
    @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
  ): ResponseEntity<Void> {
    val authenticationInfo = authentication.authenticate(authorization)
    validateScope(Scope.USER_CLIENTS_DELETE, authenticationInfo.clientScope)
    validateUserId(userId)
    userApprovedClientService.revokeClientAccess(userId, clientId)
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
  }

  private fun validateUserId(userId: String) {
    if (!userIdValidator.isValid(userId)) {
      throw ApiException("Invalid user id", BAD_REQUEST_CODE)
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
    if (size > 20 || size < 1) {
      throw ApiException("size cannot be more than 20 and less than 1", BAD_REQUEST_CODE)
    }
    return size
  }

  private fun validateScope(scope: Scope, scopes:Set<Scope>) {
    if (!scopes.contains(scope)) {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }
}
