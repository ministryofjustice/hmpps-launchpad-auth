package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserApprovedClientDto
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import java.util.*

@Service
class UserApprovedClientService(
  private var userApprovedClientRepository: UserApprovedClientRepository,
  private var clientService: ClientService,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(UserApprovedClientService::class.java)
  }

  fun upsertUserApprovedClient(userApprovedClient: UserApprovedClient): UserApprovedClient {
    logger.info("Upsert user approved client for user id:{} client id:{}", userApprovedClient.userId, userApprovedClient.clientId)
    return userApprovedClientRepository.save(userApprovedClient)
  }

  fun getUserApprovedClientById(id: UUID): Optional<UserApprovedClient> {
    logger.info("Retrieving user approved client for  id:{}", id)
    return userApprovedClientRepository.findById(id)
  }

  fun deleteUserApprovedClientById(id: UUID) {
    logger.info(String.format("Deleting user approved client for  id:%s", id))
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
    .orElseThrow{
      logger.error("Client id {} not found", clientId)
      val message = "Client id not found"
      throw ApiException("Client id not found", HttpStatus.BAD_REQUEST.value(), ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid request")
    }
    if (client.autoApprove) {
      val message = "Requested action not permitted"
      throw ApiException("Requested action not permitted", HttpStatus.BAD_REQUEST.value(), ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid request")
    }
    val userApprovedClient =
      userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(userId, clientId).orElseThrow {
        val message = String.format(
          "No record found for user id:%s and client id:%s",
          userId,
          clientId.toString(),
        )
        throw ApiException(
          message,
          HttpStatus.BAD_REQUEST.value(),
          ApiErrorTypes.INVALID_REQUEST.toString(),
          "Invalid request"
        )
      }
    userApprovedClientRepository.deleteById(userApprovedClient.id)
  }

  private fun getUserApprovedClientsDto(
    userApprovedClientPage: Page<UserApprovedClient>,
  ): PagedResult<UserApprovedClientDto> {
    val clients = ArrayList<UserApprovedClientDto>()
    userApprovedClientPage.content.forEach { userApprovedClient ->
      val client = clientService.getClientById(userApprovedClient.clientId).orElseThrow {
        val message = String.format("Client id not found %s", userApprovedClient.clientId)
        throw ApiException(message, HttpStatus.BAD_REQUEST.value(), ApiErrorTypes.INVALID_REQUEST.toString(), "Invalid request")
      }
      clients.add(
        UserApprovedClientDto(
          client.id,
          client.name,
          client.logoUri,
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

  private fun convertScopes(scopes: Set<Scope>): List<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope> {
    val scopeDto = ArrayList<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope>()
    scopes.forEach { scope ->
      scopeDto.add(
        uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope(
          scope.toString(),
          Scope.getTemplateTextByScopes(setOf(scope)).first(),
        ),
      )
    }
    return scopeDto
  }
}
