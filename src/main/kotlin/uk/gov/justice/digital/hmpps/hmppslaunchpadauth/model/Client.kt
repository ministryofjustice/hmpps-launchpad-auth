package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "client")
data class Client(
  @Id
  val id: UUID,

  @Column(name = "secret")
  val secret: String,

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "scopes")
  val scopes: Set<Scope>,

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "authorized_grant_types")
  val authorizedGrantTypes: Set<AuthorizationGrantType>,

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "registered_redirect_uris")
  val registeredRedirectUris: Set<String>,

  @Column(name = "enabled")
  val enabled: Boolean = false,

  @Column(name = "auto_approve")
  val autoApprove: Boolean = false,

  @Column(name = "name")
  val name: String,

  @Column(name = "logo_uri")
  val logoUri: String,

  @Column(name = "description")
  val description: String,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Client

    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
