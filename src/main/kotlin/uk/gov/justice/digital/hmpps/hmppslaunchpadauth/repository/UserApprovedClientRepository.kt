package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import java.util.*

@Repository
interface UserApprovedClientRepository : JpaRepository<UserApprovedClient, UUID>, PagingAndSortingRepository<UserApprovedClient, UUID> {
  fun findAllByUserId(userId: String, pageable: Pageable): List<UserApprovedClient>

  fun countAllByUserId(userId: String): Int

  fun findUserApprovedClientByUserIdAndClientId(userId: String, clientId: UUID): Optional<UserApprovedClient>
}
