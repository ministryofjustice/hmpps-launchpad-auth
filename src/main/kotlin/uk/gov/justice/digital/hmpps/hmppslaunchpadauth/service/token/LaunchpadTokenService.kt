package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

interface LaunchpadTokenService {
  fun createToken()
  fun validateTokenSignature()

  fun validateTokenClaims()
}