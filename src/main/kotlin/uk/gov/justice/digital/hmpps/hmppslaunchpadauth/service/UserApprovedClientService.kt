package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.PagedResult
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
  private val logger = LoggerFactory.getLogger(UserApprovedClientService::class.java)

  fun upsertUserApprovedClient(userApprovedClient: UserApprovedClient): UserApprovedClient {
    logger.info(
      String.format(
        "Upsert user approved client for user id:%s client id:%s",
        userApprovedClient.userId,
        userApprovedClient.clientId,
      ),
    )
    return userApprovedClientRepository.save(userApprovedClient)
  }

  fun getUserApprovedClientById(id: UUID): Optional<UserApprovedClient> {
    logger.info(String.format("Retrieving user approved client for  id:%s", id))
    return userApprovedClientRepository.findById(id)
  }

  fun deleteUserApprovedClientById(id: UUID) {
    logger.info(String.format("Deleting user approved client for  id:%s", id))
    return userApprovedClientRepository.deleteById(id)
  }

  fun getUserApprovedClientsByUserId(
    userId: String,
    page: Int,
    size: Int,
  ): PagedResult<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Client> {
    logger.debug(String.format("Getting user approved clients for user id: %s", userId))
    val pageRequest = PageRequest.of(page - 1, size)
    val totalElements = userApprovedClientRepository.countAllByUserId(userId)
    val userApprovedClients = userApprovedClientRepository.findUserApprovedClientsByUserId(userId, pageRequest)
    return getUserApprovedClientsDto(userApprovedClients.content, page, totalElements)
  }

  fun getUserApprovedClientByUserIdAndClientId(userId: String, clientId: UUID): Optional<UserApprovedClient> {
    logger.debug(String.format("Getting user approved clients for user-id: %s and client-id:%s", userId, clientId))
    return userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(userId, clientId)
  }

  fun revokeClientAccess(userId: String, clientId: UUID) {
    val userApprovedClient =
      userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(userId, clientId).orElseThrow {
        throw ApiException(
          String.format(
            "No record found for user id:%s and client id:%s",
            userId,
            clientId.toString(),
          ),
          BAD_REQUEST_CODE,
        )
      }
    userApprovedClientRepository.deleteById(userApprovedClient.id)
  }

  private fun getUserApprovedClientsDto(
    userApprovedClients: List<UserApprovedClient>,
    page: Int,
    totalElements: Int,
  ): PagedResult<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Client> {
    val clients = ArrayList<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Client>()
    userApprovedClients.forEach { x ->
      val client = clientService.getClientById(x.clientId).orElseThrow {
        throw ApiException(String.format("Client id not found %s", x.clientId), BAD_REQUEST_CODE)
      }
      clients.add(
        uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Client(
          client.id,
          client.name,
          client.logoUri,
          client.description,
          client.autoApprove,
          x.createdDate,
          convertScopes(x.scopes),
        ),
      )
    }
    return PagedResult(
      page,
      true,
      totalElements,
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
