package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_REQUEST_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserApprovedClientDto
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@Service
class UserApprovedClientService(
  private var userApprovedClientRepository: UserApprovedClientRepository,
  private var clientService: ClientService,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(UserApprovedClientService::class.java)
  }

  @Value("\${launchpad.auth.inactive-users-per-page}")
  private lateinit var inactiveUsersSizePerPage: String

  fun upsertUserApprovedClient(userApprovedClient: UserApprovedClient): UserApprovedClient {
    logger.info(
      "Upsert user approved client for user id:{} client id:{}",
      userApprovedClient.userId,
      userApprovedClient.clientId,
    )
    val scopes = ObjectMapper().writeValueAsString(userApprovedClient.scopes)
    userApprovedClientRepository.upsertUserApprovedClient(userApprovedClient.id, userApprovedClient.createdDate, userApprovedClient.lastModifiedDate, userApprovedClient.clientId, userApprovedClient.userId, scopes)
    return userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(userApprovedClient.userId, userApprovedClient.clientId).get()
  }

  fun getUserApprovedClientById(id: UUID): Optional<UserApprovedClient> {
    logger.info("Retrieving user approved client for  id:{}", id)
    return userApprovedClientRepository.findById(id)
  }

  fun deleteUserApprovedClientById(id: UUID) {
    logger.info("Deleting user approved client for  id:{}", id)
    return userApprovedClientRepository.deleteById(id)
  }

  fun getUserApprovedClientsByUserId(userId: String, page: Int, size: Int): PagedResult<UserApprovedClientDto> {
    logger.debug("Getting user approved clients for user id: {}", userId)
    val pageRequest = PageRequest.of(page - 1, size).withSort(Sort.Direction.DESC, "created_date")
    val userApprovedClientPage = userApprovedClientRepository
      .findUserApprovedClientsByUserIdAndClientIdsIsNotNull(userId, pageRequest)
    return getUserApprovedClientsDto(userApprovedClientPage)
  }

  fun getUserApprovedClientByUserIdAndClientId(userId: String, clientId: UUID): Optional<UserApprovedClient> {
    logger.debug("Getting user approved clients for user-id: {} and client-id:{}", userId, clientId)
    return userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(userId, clientId)
  }

  fun revokeClientAccess(userId: String, clientId: UUID) {
    val client = clientService.getClientById(clientId)
      .orElseThrow {
        val message = "Client id $clientId not found"
        throw ApiException(
          message,
          HttpStatus.BAD_REQUEST,
          ApiErrorTypes.INVALID_REQUEST.toString(),
          INVALID_REQUEST_MSG,
        )
      }
    if (client.autoApprove) {
      val message = "Requested action not permitted"
      throw ApiException(
        message,
        HttpStatus.BAD_REQUEST,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        INVALID_REQUEST_MSG,
      )
    }
    val userApprovedClient =
      userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(userId, clientId).orElseThrow {
        val message = "No record found for user id:$userId and client id:$clientId"
        throw ApiException(
          message,
          HttpStatus.BAD_REQUEST,
          ApiErrorTypes.INVALID_REQUEST.toString(),
          INVALID_REQUEST_MSG,
        )
      }
    userApprovedClientRepository.deleteById(userApprovedClient.id)
  }

  fun deleteInActiveUserApprovedClient() {
    logger.info("Delete User approved clients older than 7 years")
    var pageNumber = 0
    var remaining = true
    val date = LocalDateTime.now(ZoneOffset.UTC).minusYears(7L)
    val userApprovedClientsToBeDeleted = HashSet<UUID>()
    val usersToBeDeleted = HashSet<String>()
    while (remaining) {
      val pageRequest = PageRequest.of(pageNumber, inactiveUsersSizePerPage.toInt())
      val pageResult = userApprovedClientRepository
        .findUserApprovedClientsByLastModifiedDateIsLessThan(date, pageRequest)
      pageResult.content.forEach { x ->
        if (!usersToBeDeleted.contains(x.userId)) {
          val usersApprovedClients = userApprovedClientRepository.findUserApprovedClientsByUserId(x.userId)
          val filtered =
            usersApprovedClients.filter { userApprovedClient -> userApprovedClient.lastModifiedDate.isBefore(date) }
          if (usersApprovedClients.size == filtered.size) {
            usersToBeDeleted.add(usersApprovedClients[0].userId)
            usersApprovedClients.forEach { userApprovedClient -> userApprovedClientsToBeDeleted.add(userApprovedClient.id) }
          }
        }
      }
      remaining = !pageResult.isLast
      pageNumber += 1
    }
    logger.info(
      "Number of user approved clients to be deleted: '{}' and number of users to be deleted: '{}'",
      userApprovedClientsToBeDeleted.size,
      usersToBeDeleted.size,
    )
    userApprovedClientRepository.deleteAllById(userApprovedClientsToBeDeleted)
  }

  private fun getUserApprovedClientsDto(
    userApprovedClientPage: Page<UserApprovedClient>,
  ): PagedResult<UserApprovedClientDto> {
    val clients = ArrayList<UserApprovedClientDto>()
    userApprovedClientPage.content.forEach { userApprovedClient ->
      val client = clientService.getClientById(userApprovedClient.clientId).orElseThrow {
        val message = "Client id ${userApprovedClient.clientId} not found"
        throw ApiException(
          message,
          HttpStatus.BAD_REQUEST,
          ApiErrorTypes.INVALID_REQUEST.toString(),
          INVALID_REQUEST_MSG,
        )
      }
      var logoUri: String? = null
      if (!client.logoUri.isNullOrEmpty()) {
        logoUri = client.logoUri
      }
      clients.add(
        UserApprovedClientDto(
          client.id,
          client.name,
          logoUri,
          client.description,
          client.autoApprove,
          userApprovedClient.createdDate,
          convertScopes(userApprovedClient.scopes),
        ),
      )
    }
    return PagedResult(
      userApprovedClientPage.number + 1,
      userApprovedClientPage.isLast,
      userApprovedClientPage.totalElements,
      clients,
    )
  }

  private fun convertScopes(scopes: Set<Scope>): List<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope> = Scope.getScopeDtosByScopes(scopes)
}
