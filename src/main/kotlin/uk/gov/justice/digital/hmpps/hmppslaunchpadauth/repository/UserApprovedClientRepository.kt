package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import java.util.*

@Repository
interface UserApprovedClientRepository : JpaRepository<UserApprovedClient, UUID>, PagingAndSortingRepository<UserApprovedClient, UUID> {
  //@Query(value = "select u from UserApprovedClient u where u.userId =:userId")
  fun findAllByUserId(userId: String, pageable: Pageable): List<UserApprovedClient>

  fun findUserApprovedClientByUserIdAndClientId(userId: String, clientId: UUID): Optional<UserApprovedClient>
}
