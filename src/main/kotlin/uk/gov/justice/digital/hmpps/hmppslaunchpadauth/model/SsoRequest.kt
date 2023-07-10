package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(indexes = [
  Index(name="ix_authorization_code", columnList = "authorizationCode", unique = true)
])
data class SsoRequest(
  @Id
  val id: UUID,
  val nonce: String,
  val createdDate: LocalDateTime,
  var authorizationCode: UUID,
  @JdbcTypeCode(SqlTypes.JSON)
  val client: SsoClient,
  var userId: String?,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SsoRequest

    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}

class SsoClient(
  @JsonProperty("id")
  val id: UUID,
  @JsonProperty("state")
  val state: String?,
  @JsonProperty("nonce")
  val nonce: String?,
  @JsonProperty("scopes")
  val scopes: Set<Scope>,
  @JsonProperty("redirectUri")
  val redirectUri: String,
)
