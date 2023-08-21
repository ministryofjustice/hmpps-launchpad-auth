package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.slf4j.LoggerFactory
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
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.ACCESS_DENIED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.ACCESS_DENIED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.BAD_REQUEST_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserApprovedClientDto
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.UserApprovedClientService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.Authentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.AuthenticationUserInfo
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.util.*

@RestController
@RequestMapping("/v1")
class UserApprovedClientController(
  @Qualifier("tokenAuthentication") private var authentication: Authentication,
  private var userApprovedClientService: UserApprovedClientService,
  private var userIdValidator: UserIdValidator
) {
  companion object {
    private val logger = LoggerFactory.getLogger(UserApprovedClientController::class.java)
  }

  @GetMapping("/users/{user-id}/clients", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getUserApprovedClients(
    @PathVariable("user-id") userId: String,
    @RequestParam("page", required = false) page: Int?,
    @RequestParam("size", required = false) size: Int?,
    @RequestHeader(HttpHeaders.AUTHORIZATION, required = true) authorization: String,
  ): ResponseEntity<PagedResult<UserApprovedClientDto>> {
    val authenticationInfo = authentication.authenticate(authorization) as AuthenticationUserInfo
    validateScope(Scope.USER_CLIENTS_READ, authenticationInfo.userApprovedScope)
    validateUserId(userId, authenticationInfo.userId)
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
    @RequestHeader(HttpHeaders.AUTHORIZATION, required = true) authorization: String,
  ): ResponseEntity<Void> {
    val authenticationInfo = authentication.authenticate(authorization) as AuthenticationUserInfo
    validateScope(Scope.USER_CLIENTS_DELETE, authenticationInfo.userApprovedScope)
    validateUserId(userId, authenticationInfo.userId)
    userApprovedClientService.revokeClientAccess(userId, clientId)
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
  }


  private fun validateUserId(userId: String, userIdFromToken: String) {
    if (userId != userIdFromToken) {
      val message  = String.format("User id %s in token do not match with user id %s in api path", userIdFromToken, userId)
      logger.error(message)
      throw ApiException(message, 400, ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid user id in api path")
    }
    if (!userIdValidator.isValid(userId)) {
      val message = String.format("invalid user id format %s", userId)
      logger.error(message)
      throw ApiException(message, BAD_REQUEST_CODE, ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid user id format")
    }
  }

  private fun validatePage(page: Int?): Int {
    if (page == null) {
      return 1
    }
    if (page < 1) {
      val message = "page cannot be less than 1"
      throw ApiException(message, BAD_REQUEST_CODE, ApiErrorTypes.INVALID_REQUEST.toString(), message)
    }
    return page
  }

  private fun validatePageSize(size: Int?): Int {
    if (size == null) {
      return 20
    }
    if (size > 20 || size < 1) {
      val message = "size cannot be more than 20 and less than 1"
      throw ApiException(message, BAD_REQUEST_CODE, ApiErrorTypes.INVALID_REQUEST.toString(), message)
    }
    return size
  }

  private fun validateScope(scope: Scope, scopes:Set<Scope>) {
    if (!scopes.contains(scope)) {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE, ApiErrorTypes.INVALID_SCOPE.toString(), "Invalid scope")
    }
  }
}
