package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception

import java.net.URI

class SsoException(
  override val message: String,
  override val code: Int,
  override val error: String,
  override val errorDescription: String,
  val redirectUri: String
): ApiException(message, code, error, errorDescription) {
}