package uk.gov.justice.digital.hmpps.utils

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import java.util.UUID

class DataGenerator {
  companion object {
    fun buildClient(): Client {
      val client = Client()
      client.id = UUID.randomUUID()
      client.scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ)
      client.authorizedGrantTypes = setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN)
      client.autoApprove = true
      client.enabled = true
      client.logoUri = "http://localhost:8080/test"
      client.registeredRedirectUris = setOf("http://localhost:8080/test")
      client.name = "Test App"
      client.secret = UUID.randomUUID().toString()
      client.description = "Test App"
      return client
    }
  }
}
