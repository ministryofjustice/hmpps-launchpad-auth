package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant

import org.springframework.http.HttpHeaders


class AuthServiceConstant {
  companion object {
    const val CODE = "code"
    const val INVALID_CODE_MSG = "Invalid code"
    const val INVALID_CLIENT_ID = "Invalid client id"
    const val ACCESS_DENIED_MSG = "Permission denied"
    const val INVALID_SCOPE = "The requested scope is invalid or not found."
    const val INVALID_RESPONSE_TYPE_MSG = "The requested response type is invalid"
    const val INVALID_REQUEST_MSG = "Invalid request"
    const val INVALID_GRANT_TYPE_MSG  = "The requested grant is invalid"
    const val INVALID_REDIRECT_URI_MSG = "The requested redirect uri is invalid or not found"
    const val UNAUTHORIZED_MSG = "Unauthorized"
    const val EXPIRE_TOKEN_MSG = "Token has expired"
    const val INVALID_TOKEN_MSG = "Invalid token"
    const val REDIRECTION_CODE = 302
    fun getResponseHeaders() : HttpHeaders {
      val httpHeaders: HttpHeaders = HttpHeaders()
      httpHeaders.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate")
      httpHeaders.add(HttpHeaders.PRAGMA, "no-cache")
      httpHeaders.add(HttpHeaders.EXPIRES, "0")
      httpHeaders.add("X-Frame-Options", "DENY")
      httpHeaders.add("X-Content-Type-Options", "nosniff")
      return httpHeaders
    }
  }
}