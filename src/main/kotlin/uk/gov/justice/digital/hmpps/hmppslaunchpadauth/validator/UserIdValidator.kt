package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator

import org.springframework.stereotype.Component

@Component
class UserIdValidator : Validator<String> {

  override fun isValid(source: String): Boolean {
    return "^[A-Z][0-9]{4}[A-Z]{2}$".toRegex().matches(source)
  }
}
