package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import java.time.LocalDate

@Service
class SarService(private var userApprovedClientRepository: UserApprovedClientRepository) {

  fun getUsers(
    userId: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): List<UserApprovedClient> {
    var userApprovedClients: List<UserApprovedClient>
    if (fromDate != null && toDate == null) {
      // get users from created time greater than equal to
      userApprovedClients = userApprovedClientRepository.findUserApprovedClientsByUserIdAndCreatedDateGreaterThanEqual(userId, fromDate.atStartOfDay())
    } else if (fromDate == null && toDate != null) {
      // get users from last modified time less than equal to
      userApprovedClients = userApprovedClientRepository.findUserApprovedClientsByUserIdAndLastModifiedDateGreaterThanEqual(userId, toDate.atStartOfDay())
    } else if (fromDate != null && toDate != null) {
      // get users from created time greater than equal to
      userApprovedClients = userApprovedClientRepository.findUserApprovedClientsByUserIdAndCreatedDateGreaterThanEqualAndLastModifiedDateLessThanEqual(userId, fromDate.atStartOfDay(), toDate.atStartOfDay())
    } else {
      // get users by just user id
      userApprovedClients = userApprovedClientRepository.findUserApprovedClientsByUserId(userId)
    }
    return userApprovedClients
  }
}