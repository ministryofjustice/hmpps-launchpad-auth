package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import java.util.*

@Repository
interface UserApprovedClientRepository : JpaRepository<UserApprovedClient, UUID> {
  fun findAllByUserId(userId: String, pageable: Pageable): Page<UserApprovedClient>

  fun countAllByUserId(userId: String): Int

  fun findUserApprovedClientByUserIdAndClientId(userId: String, clientId: UUID): Optional<UserApprovedClient>
}
