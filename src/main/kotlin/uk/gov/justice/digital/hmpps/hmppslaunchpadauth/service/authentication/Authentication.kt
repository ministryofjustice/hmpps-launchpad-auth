package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

interface Authentication {
  fun authenticate(credential: String): AuthenticationInfo
}
