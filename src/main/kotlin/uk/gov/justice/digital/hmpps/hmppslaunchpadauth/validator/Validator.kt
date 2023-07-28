package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator

interface Validator<S> {
  fun isValid(source: S): Boolean
}
