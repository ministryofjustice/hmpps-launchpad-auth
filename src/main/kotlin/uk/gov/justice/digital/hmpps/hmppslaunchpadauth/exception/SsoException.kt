package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception

import org.springframework.http.HttpStatus

class SsoException(
  override val message: String,
  override val code: HttpStatus,
  override val error: String,
  override val errorDescription: String,
  val redirectUri: String,
  val state: String?,
) : ApiException(message, code, error, errorDescription)
