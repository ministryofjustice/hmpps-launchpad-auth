package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant

class AuthServiceConstant {
  companion object {
    const val CODE = "code"
    const val INVALID_CODE_MSG = "Invalid code"
    const val INVALID_CLIENT_ID_MSG = "Invalid client id"
    const val INTERNAL_SERVER_ERROR_MSG = "Server error"
    const val ACCESS_DENIED_MSG = "Permission denied"
    const val INVALID_SCOPE_MSG = "The requested scope is invalid or not found"
    const val INVALID_RESPONSE_TYPE_MSG = "The requested response type is invalid"
    const val INVALID_REQUEST_MSG = "Invalid request"
    const val INVALID_GRANT_TYPE_MSG = "The requested grant is invalid"
    const val INVALID_REDIRECT_URI_MSG = "The requested redirect uri is invalid or not found"
    const val EXPIRE_TOKEN_MSG = "Token has expired"
    const val INVALID_TOKEN_MSG = "Invalid token"
  }
}
