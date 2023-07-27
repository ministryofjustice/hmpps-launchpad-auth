package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
  name = "user_approved_client",
  indexes = [
    Index(name = "ix_created_date_user_id_client_id", columnList = " created_date,user_id,client_id", unique = true),
  ],
)
data class UserApprovedClient(
  @Id
  val id: UUID,

  @Column(name = "user_id", nullable = false)
  val userId: String,

  @Column(name = "client_id", nullable = false)
  val clientId: UUID,

  @Column(name = "scopes", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  var scopes: Set<Scope>,

  @Column(name = "created_date", nullable = false)
  val createdDate: LocalDateTime,

  @Column(name = "last_modified_date", nullable = false)
  var lastModifiedDate: LocalDateTime,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as UserApprovedClient

    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
