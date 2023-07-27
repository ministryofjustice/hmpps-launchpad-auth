package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import java.util.*

@Repository
interface UserApprovedClientRepository : JpaRepository<UserApprovedClient, UUID> {
  @Query("SELECT u FROM UserApprovedClient u where u.userId = :userId AND u.clientId is not null")
  fun findUserApprovedClientsByUserIdAndClientIdsIsNotNull(@Param("userId") userId: String, pageable: Pageable): Page<UserApprovedClient>

  fun findUserApprovedClientByUserIdAndClientId(userId: String, clientId: UUID): Optional<UserApprovedClient>
}
