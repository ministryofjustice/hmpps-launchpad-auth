package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import java.time.LocalDateTime
import java.util.*

@Repository
interface SsoRequestRepository : JpaRepository<SsoRequest, UUID> {
  fun findSsoRequestByAuthorizationCode(code: UUID): Optional<SsoRequest>

  @Transactional
  fun deleteSsoRequestByCreatedDateIsLessThan(date: LocalDateTime)
}
