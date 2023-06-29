package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

interface TokenProcessor {
  fun getUserId(token: String): String

}