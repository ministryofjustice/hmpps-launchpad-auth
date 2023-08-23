package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.view.RedirectView
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.ApiError
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException

@RestControllerAdvice
class HmppsLaunchpadAuthExceptionHandler {

  @ExceptionHandler(ApiException::class)
  fun handleSingleSignOnException(e: ApiException): ResponseEntity<ApiError> {
    log.error("Api Exception: {}", e.message)
    return ResponseEntity
      .status(e.code)
      .body(
        ApiError(
          e.error,
          e.errorDescription,
        ),
      )
  }

  @ExceptionHandler(SsoException::class)
  fun handleSingleSignOnException(e: SsoException): RedirectView {
    log.error("Single sign on exception: {}", e.message)
    return RedirectView("${e.redirectUri}?error=${e.error}&error_description=${e.errorDescription}")
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ApiError?>? {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ApiError(
          error = ApiErrorTypes.SERVER_ERROR.toString(),
          errorDescription = "Internal Server Error",
        ),
      )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

