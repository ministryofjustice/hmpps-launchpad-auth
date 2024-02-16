package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.analytics

import java.time.LocalDateTime

data class SignedInUser(
  val userId: String,
  val dateTime: LocalDateTime,
  val clientId: String,
  val establishment: String,
)
