package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import java.util.*
@Entity
class Client {
  @Id
  lateinit var id: UUID
  lateinit var secret: String
  @ElementCollection(targetClass = Scope::class, fetch = FetchType.EAGER)
  @Enumerated(value = EnumType.STRING)
  lateinit var scopes: Set<Scope>
  @ElementCollection(targetClass = AuthorizationGrantType::class, fetch = FetchType.EAGER)
  @Enumerated(value = EnumType.STRING)
  @Column(name = "authorized_grant_types")
  lateinit var authorizedGrantTypes: Set<AuthorizationGrantType>
  @ElementCollection(targetClass = String::class, fetch = FetchType.EAGER)
  @Enumerated(value = EnumType.STRING)
  @Column(name = "registered_redirect_uris")
  lateinit var registeredRedirectUris: Set<String>
  var enabled: Boolean = false;
  var autoApprove: Boolean = false
  lateinit var name: String
  @Column(name = "logo_uri")
  lateinit var logoUri: String
  lateinit var description: String
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Client

    if (id != other.id) return false
    return secret == other.secret
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + secret.hashCode()
    return result
  }


}