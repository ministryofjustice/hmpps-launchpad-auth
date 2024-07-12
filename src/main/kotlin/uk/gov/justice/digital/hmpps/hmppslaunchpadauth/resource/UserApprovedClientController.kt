package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
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
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.ACCESS_DENIED_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_CLIENT_ID_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_SCOPE_MSG
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
  private var userIdValidator: UserIdValidator,
) {

  @Tag(name = "get list of a user approved clients", description = "Give list of user approved clients by user id")
  @GetMapping("/users/{user-id}/clients", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getUserApprovedClients(
    @PathVariable("user-id") userId: String,
    @RequestParam("page", required = false) page: Long?,
    @RequestParam("size", required = false) size: Long?,
    @RequestHeader(HttpHeaders.AUTHORIZATION, required = true) authorization: String,
  ): ResponseEntity<PagedResult<UserApprovedClientDto>> {
    val authenticationInfo = authentication.authenticate(authorization) as AuthenticationUserInfo
    validateUserId(userId, authenticationInfo.userId)
    validateScope(Scope.USER_CLIENTS_READ, authenticationInfo.userApprovedScope)
    val pageNum = validatePage(page)
    val pageSize = validatePageSize(size)
    val userApprovedClients = userApprovedClientService
      .getUserApprovedClientsByUserId(userId, pageNum.toInt(), pageSize.toInt())
    return ResponseEntity.status(HttpStatus.OK).body(userApprovedClients)
  }

  @Tag(name = "revoke client access", description = "Revoke a previously approved client by a user")
  @DeleteMapping("/users/{user-id}/clients/{client-id}")
  fun revokeClientAccess(
    @PathVariable("user-id") userId: String,
    @PathVariable("client-id") clientId: UUID,
    @RequestHeader(HttpHeaders.AUTHORIZATION, required = true) authorization: String,
  ): ResponseEntity<Void> {
    val authenticationInfo = authentication.authenticate(authorization) as AuthenticationUserInfo
    validateUserId(userId, authenticationInfo.userId)
    validateClientId(clientId, authenticationInfo.clientId)
    validateScope(Scope.USER_CLIENTS_DELETE, authenticationInfo.userApprovedScope)
    userApprovedClientService.revokeClientAccess(userId, clientId)
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
  }

  private fun validateClientId(clientId: UUID, clientIdFromToken: UUID) {
    if (!clientId.equals(clientIdFromToken)) {
      val message = "Client id $clientId in api path do not match with client id $clientIdFromToken in token"
      throw ApiException(
        message,
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        INVALID_CLIENT_ID_MSG,
      )
    }
  }

  private fun validateUserId(userId: String, userIdFromToken: String) {
    if (userId != userIdFromToken) {
      val message = "User id $userIdFromToken in token do not match with user id $userId in api path"
      throw ApiException(message, HttpStatus.BAD_REQUEST, ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid user id in api path")
    }
    if (!userIdValidator.isValid(userId)) {
      val message = "Invalid user id format $userId"
      throw ApiException(
        message,
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        "Invalid user id format",
      )
    }
  }

  private fun validatePage(page: Long?): Long {
    if (page == null) {
      return 1
    }
    if (page >= Int.MAX_VALUE) {
      val message = "page cannot have value $page"
      throw ApiException(message, HttpStatus.BAD_REQUEST, ApiErrorTypes.INVALID_REQUEST.toString(), message)
    }
    if (page < 1) {
      val message = "page cannot be less than 1"
      throw ApiException(message, HttpStatus.BAD_REQUEST, ApiErrorTypes.INVALID_REQUEST.toString(), message)
    }
    return page
  }

  private fun validatePageSize(size: Long?): Long {
    if (size == null) {
      return 20
    }
    if (size > 20 || size < 1) {
      val message = "size cannot be more than 20 and less than 1"
      throw ApiException(message, HttpStatus.BAD_REQUEST, ApiErrorTypes.INVALID_REQUEST.toString(), message)
    }
    return size
  }

  private fun validateScope(scope: Scope, scopes: Set<Scope>) {
    if (!scopes.contains(scope)) {
      throw ApiException(
        ACCESS_DENIED_MSG,
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.INVALID_SCOPE.toString(),
        INVALID_SCOPE_MSG,
      )
    }
  }
}
