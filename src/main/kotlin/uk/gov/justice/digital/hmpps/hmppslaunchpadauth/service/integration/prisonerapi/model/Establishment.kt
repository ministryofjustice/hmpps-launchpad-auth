package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

data class Establishment(
  val id: String,
  val agencyId: String,
  val name: String,
  val displayName: String,
  val youth: Boolean
)
