package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception

import org.springframework.http.HttpStatus

open class ApiException(
  override val message: String,
  open val code: HttpStatus,
  open val error: String,
  open val errorDescription: String,
) : RuntimeException(message)
