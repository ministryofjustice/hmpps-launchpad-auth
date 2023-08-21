package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
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
  fun handleApiException(e: ApiException): ResponseEntity<ApiError> {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(e.code)
      .body(
        ApiError(
          error = e.error,
          errorDescription = e.errorDescription,
        ),
      )
  }

  @ExceptionHandler(SsoException::class)
  fun handleApiException(e: SsoException): RedirectView {
    log.info("Validation exception: {}", e.message)
    return RedirectView("${e.redirectUri}?error=${e.error}&error_description=${e.errorDescription}")
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ApiError?>? {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ApiError(
          error = ApiErrorTypes.SERVER_ERROR.toString(),
          errorDescription = "Internal Server ERROR",
        ),
      )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
