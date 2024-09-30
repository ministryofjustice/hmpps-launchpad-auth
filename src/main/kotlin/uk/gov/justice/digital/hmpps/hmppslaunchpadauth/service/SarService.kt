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
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate
import java.time.chrono.ChronoLocalDateTime

@Service
class SarService(
  private var userApprovedClientRepository: UserApprovedClientRepository,
  private var clientService: ClientService,
) : HmppsPrisonSubjectAccessRequestService {

  private fun getUsers(
    userId: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): List<UserApprovedClientDto> {
    var sarContent = ArrayList<UserApprovedClient>()
    var userApprovedClients: List<UserApprovedClient> =
      userApprovedClientRepository.findUserApprovedClientsByUserId(userId)
    userApprovedClients.forEach { userApprovedClient ->
      if (fromDate != null && toDate == null) {
        if (userApprovedClient.createdDate.isAfter(ChronoLocalDateTime.from(fromDate.atStartOfDay()))) {
          sarContent.add(userApprovedClient)
        }
      } else if (fromDate == null && toDate != null) {
        if (userApprovedClient.lastModifiedDate.isBefore(ChronoLocalDateTime.from(toDate.atStartOfDay()))) {
          sarContent.add(userApprovedClient)
        }
      } else if (fromDate != null && toDate != null) {
        if (
          !userApprovedClient.createdDate.isAfter(ChronoLocalDateTime.from(fromDate.atStartOfDay())) &&
          !userApprovedClient.lastModifiedDate.isBefore(ChronoLocalDateTime.from(toDate.atStartOfDay()))
        ) {
          sarContent.add(userApprovedClient)
        }
      } else {
        sarContent.add(userApprovedClient)
      }
    }
    return getUserApprovedClientsDto(sarContent)
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

  override fun getPrisonContentFor(
    prn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val users = getUsers(prn, fromDate, toDate)
    return HmppsSubjectAccessRequestContent(users)
  }
}
