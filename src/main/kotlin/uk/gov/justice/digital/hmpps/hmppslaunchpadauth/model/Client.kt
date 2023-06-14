package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "client")
class Client {
  @Id
  lateinit var id: UUID
  lateinit var secret: String

  @ElementCollection(targetClass = Scope::class, fetch = FetchType.EAGER)
  @Enumerated(value = EnumType.STRING)
  @Column(name = "scopes")
  lateinit var scopes: Set<Scope>

  @ElementCollection(targetClass = AuthorizationGrantType::class, fetch = FetchType.EAGER)
  @Enumerated(value = EnumType.STRING)
  @Column(name = "authorized_grant_types")
  lateinit var authorizedGrantTypes: Set<AuthorizationGrantType>

  @ElementCollection(targetClass = String::class, fetch = FetchType.EAGER)
  @Enumerated(value = EnumType.STRING)
  @Column(name = "registered_redirect_uris")
  lateinit var registeredRedirectUris: Set<String>

  @Column(name = "enabled")
  var enabled: Boolean = false

  @Column(name = "auto_approve")
  var autoApprove: Boolean = false
  lateinit var name: String

  @Column(name = "logo_uri")
  lateinit var logoUri: String

  @Column(name = "description")
  lateinit var description: String
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Client

    if (id != other.id) return false
    if (secret != other.secret) return false
    if (scopes != other.scopes) return false
    if (authorizedGrantTypes != other.authorizedGrantTypes) return false
    if (registeredRedirectUris != other.registeredRedirectUris) return false
    if (enabled != other.enabled) return false
    if (autoApprove != other.autoApprove) return false
    if (name != other.name) return false
    if (logoUri != other.logoUri) return false
    return description == other.description
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + secret.hashCode()
    result = 31 * result + scopes.hashCode()
    result = 31 * result + authorizedGrantTypes.hashCode()
    result = 31 * result + registeredRedirectUris.hashCode()
    result = 31 * result + enabled.hashCode()
    result = 31 * result + autoApprove.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + logoUri.hashCode()
    result = 31 * result + description.hashCode()
    return result
  }
}
