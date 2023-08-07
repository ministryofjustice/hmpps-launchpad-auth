package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

data class PrisonerData(
  val booking: Booking,
  val establishment: Establishment,
  val profile: Profile
)
