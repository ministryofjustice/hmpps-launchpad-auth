package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

data class OffenderBooking(
  val offenderId: String,
  val bookingId: String,
  val firstName: String,
  val lastName: String,
  val agencyId: String,
)
