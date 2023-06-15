package uk.gov.justice.digital.hmpps.utils

import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import java.util.UUID

class DataGenerator {
  companion object {
    fun buildClient(): Client {
      return Client(
        UUID.randomUUID(),
        UUID.randomUUID().toString(),
        setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ),
        setOf(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN),
        setOf("http://localhost:8080/test"),
        true,
        true,
        "Test App",
        "http://localhost:8080/test",
        "Update Test App",
      )
    }
  }
}
