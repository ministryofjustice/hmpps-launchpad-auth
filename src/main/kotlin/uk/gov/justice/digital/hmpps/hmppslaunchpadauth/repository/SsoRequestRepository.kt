package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import java.util.*
@Repository
interface SsoRequestRepository : JpaRepository<SsoRequest, UUID> {
  @Query("SELECT count(r.authorizationCode) from SsoRequest r where r.authorizationCode =:code")
  fun countAuthorizationCodeByValue(@Param("code") code: UUID): Int
}
