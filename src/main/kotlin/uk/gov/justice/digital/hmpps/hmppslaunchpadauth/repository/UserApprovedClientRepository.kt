package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import java.time.LocalDateTime
import java.util.*

@Repository
interface UserApprovedClientRepository : JpaRepository<UserApprovedClient, UUID> {
  @Query(
    value = "SELECT * FROM user_approved_client WHERE user_id = ?1 AND client_id IS NOT NULL",
    countQuery = "SELECT count(*) FROM user_approved_client WHERE user_id = ?1 AND client_id IS NOT NULL",
    nativeQuery = true,
  )
  fun findUserApprovedClientsByUserIdAndClientIdsIsNotNull(userId: String, pageable: Pageable): Page<UserApprovedClient>

  fun findUserApprovedClientByUserIdAndClientId(userId: String, clientId: UUID): Optional<UserApprovedClient>

  @Transactional
  @Modifying
  @Query(value =  "delete from user_approved_client  where last_modified_date < ?1", nativeQuery = true)
  fun deleteInactiveUsersApprovedClient(date: LocalDateTime)
}
