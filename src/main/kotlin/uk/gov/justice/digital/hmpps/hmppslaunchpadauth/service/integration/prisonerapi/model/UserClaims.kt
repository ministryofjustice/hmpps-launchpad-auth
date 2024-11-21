package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

data class UserClaims(
  val booking: Booking?,
  val establishment: Establishment?,
  val user: User,
)
