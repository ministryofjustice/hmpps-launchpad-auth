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
    value = "INSERT INTO user_approved_client(id, created_date, last_modified_date, client_id, user_id, scopes)" +
      " VALUES (:id, :createdDate, :lastModifiedDate, :clientId, :userId, cast(:scopes as jsonb)) " +
      " ON CONFLICT (client_id, user_id)" +
      " DO UPDATE SET" +
      " last_modified_date = excluded.last_modified_date," +
      " scopes = excluded.scopes;",
    nativeQuery = true,
  )
  @Modifying
  @Transactional
  fun upsertUserApprovedClient(id: UUID, createdDate: LocalDateTime, lastModifiedDate: LocalDateTime, clientId: UUID, userId: String, scopes: String)

  @Query(
    value = "SELECT * FROM user_approved_client WHERE user_id = ?1 AND client_id IS NOT NULL",
    countQuery = "SELECT count(*) FROM user_approved_client WHERE user_id = ?1 AND client_id IS NOT NULL",
    nativeQuery = true,
  )
  fun findUserApprovedClientsByUserIdAndClientIdsIsNotNull(userId: String, pageable: Pageable): Page<UserApprovedClient>

  fun findUserApprovedClientByUserIdAndClientId(userId: String, clientId: UUID): Optional<UserApprovedClient>

  fun findUserApprovedClientsByLastModifiedDateIsLessThan(date: LocalDateTime, pageable: Pageable): Page<UserApprovedClient>

  fun findUserApprovedClientsByUserId(userId: String): List<UserApprovedClient>
}
