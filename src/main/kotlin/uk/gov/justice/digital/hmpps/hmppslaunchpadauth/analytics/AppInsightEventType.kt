package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.analytics

enum class AppInsightEventType {
  TOKEN_GENERATED_VIA_AUTHORIZATION_CODE,
  TOKEN_GENERATED_VIA_REFRESH_TOKEN,
  LOGIN_SUCCESSFUL_BUT_PRISONER_RECORD_NOT_FOUND,
}
