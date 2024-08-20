package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserApprovedClientDto
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import java.time.LocalDate

@Service
class SarService(
  private var userApprovedClientRepository: UserApprovedClientRepository,
  private var clientService: ClientService,
) {

  fun getUsers(
    userId: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): List<UserApprovedClientDto> {
    var userApprovedClients: List<UserApprovedClient>
    if (fromDate != null && toDate == null) {
      userApprovedClients = userApprovedClientRepository.findUserApprovedClientsByUserIdAndCreatedDateIsGreaterThanEqual(
        userId,
        fromDate.atStartOfDay(),
      )
    } else if (fromDate == null && toDate != null) {
      userApprovedClients =
        userApprovedClientRepository.findUserApprovedClientsByUserIdAndLastModifiedDateIsLessThanEqual(
          userId,
          toDate.plusDays(1).atStartOfDay(),
        )
    } else if (fromDate != null && toDate != null) {
      userApprovedClients =
        userApprovedClientRepository.findUserApprovedClientsByUserIdAndCreatedDateIsGreaterThanEqualAndLastModifiedDateIsLessThanEqual(
          userId,
          fromDate.atStartOfDay(),
          toDate.plusDays(1).atStartOfDay(),
        )
    } else {
      userApprovedClients = userApprovedClientRepository.findUserApprovedClientsByUserId(userId)
    }
    return getUserApprovedClientsDto(userApprovedClients)
  }

  private fun getUserApprovedClientsDto(
    userApprovedClients: List<UserApprovedClient>,
  ): List<UserApprovedClientDto> {
    val userApprovedClientDtos = ArrayList<UserApprovedClientDto>()
    userApprovedClients.forEach { userApprovedClient ->
      val client = clientService.getClientById(userApprovedClient.clientId).orElseThrow {
        val message = "Client id ${userApprovedClient.clientId} not found"
        throw ApiException(
          message,
          HttpStatus.BAD_REQUEST,
          ApiErrorTypes.INVALID_REQUEST.toString(),
          AuthServiceConstant.INVALID_REQUEST_MSG,
        )
      }
      var logoUri: String? = null
      if (!client.logoUri.isNullOrEmpty()) {
        logoUri = client.logoUri
      }
      userApprovedClientDtos.add(
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
    return userApprovedClientDtos
  }

  private fun convertScopes(scopes: Set<Scope>): List<uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Scope> {
    return Scope.getScopeDtosByScopes(scopes)
  }
}
