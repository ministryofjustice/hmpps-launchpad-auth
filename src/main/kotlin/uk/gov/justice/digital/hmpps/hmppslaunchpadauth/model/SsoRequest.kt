package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

@Entity
data class SsoRequest(
  @Id
  val id: UUID,
  val nonce: String?,
  val createdDate: LocalDateTime,
  var authorizationCode: UUID?,
  @JdbcTypeCode(SqlTypes.JSON)
  val client: SsoClient,
  val userId: String?,
)

class SsoClient(
  @JsonProperty("id")
  val id: UUID,
  @JsonProperty("state")
  val state: String?,
  @JsonProperty("nonce")
  val nonce: String?,
  @JsonProperty("scopes")
  val scopes: Set<Scope>,
  @JsonProperty("reDirectUri")
  val reDirectUri: String,
)
