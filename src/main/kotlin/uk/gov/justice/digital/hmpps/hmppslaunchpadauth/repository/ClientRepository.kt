package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import java.util.UUID
@Repository
interface ClientRepository : JpaRepository<Client, UUID>
