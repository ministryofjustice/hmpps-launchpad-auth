package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception

open class ApiException(
  override val message: String,
  open val code: Int,
  open val error: String,
  open val errorDescription: String,
) : RuntimeException(message)
