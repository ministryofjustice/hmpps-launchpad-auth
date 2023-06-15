package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception

class ApiException(override var message: String, var code: Int) : RuntimeException(message)
