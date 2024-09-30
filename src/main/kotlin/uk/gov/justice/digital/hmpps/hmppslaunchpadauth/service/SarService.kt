package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.HumanReadable
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.SarContentDto
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
class SarService(
  private var userApprovedClientRepository: UserApprovedClientRepository,
  private var clientService: ClientService,
) : HmppsPrisonSubjectAccessRequestService {

  private fun getUsers(
    userId: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): List<SarContentDto> {
    var userApprovedClients: List<UserApprovedClient> =
      userApprovedClientRepository.findUserApprovedClientsByUserId(userId)
    var sarContent = userApprovedClients.stream().filter { userApprovedClient ->
      (fromDate == null || fromDate.atStartOfDay() <= userApprovedClient.lastModifiedDate) &&
        (toDate == null || toDate.atStartOfDay() >= userApprovedClient.lastModifiedDate)
    }.toList()
    return getUserApprovedClientsDto(sarContent)
  }

  private fun getUserApprovedClientsDto(
    userApprovedClients: List<UserApprovedClient>,
  ): List<SarContentDto> {
    val sarContentDtos = ArrayList<SarContentDto>()
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
      sarContentDtos.add(
        SarContentDto(
          client.id,
          client.name,
          userApprovedClient.createdDate,
          userApprovedClient.lastModifiedDate,
          convertScopes(userApprovedClient.scopes),
        ),
      )
    }
    return sarContentDtos
  }

  private fun convertScopes(scopes: Set<Scope>): List<HumanReadable> {
    val humanReadables = ArrayList<HumanReadable>()
    Scope.getTemplateTextByScopes(scopes).forEach { scope ->
      if (!scope.isNullOrEmpty()) {
        humanReadables.add(HumanReadable(scope))
      }
    }
    return humanReadables
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
