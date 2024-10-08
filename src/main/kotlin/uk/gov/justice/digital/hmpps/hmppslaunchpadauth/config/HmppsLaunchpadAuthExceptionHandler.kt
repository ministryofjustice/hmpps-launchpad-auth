package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import jakarta.servlet.ServletException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.ApiError
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException
import java.util.*

@RestControllerAdvice
class HmppsLaunchpadAuthExceptionHandler {

  @ExceptionHandler(ApiException::class)
  fun handleApiException(e: ApiException): ResponseEntity<ApiError> {
    log.error("Api Exception: {} {}", e.message, e.cause)
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
    log.error("Single sign on exception: {} {}", e.message, e.cause)
    val url = UriComponentsBuilder.fromHttpUrl(e.redirectUri)
      .queryParam("error", e.error)
      .queryParam("error_description", e.errorDescription)
      .queryParamIfPresent("state", Optional.ofNullable(e.state))
      .build().toUriString()
    return RedirectView(url)
  }

  @ExceptionHandler(MissingRequestHeaderException::class)
  fun handleApiMissingRequestHeader(e: MissingRequestHeaderException): ResponseEntity<ApiError> {
    log.error("Exception due to missing required header in api request: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST.value())
      .body(
        ApiError(
          error = ApiErrorTypes.INVALID_REQUEST.toString(),
          errorDescription = "${e.message}",
        ),
      )
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
  fun handleApiMediaTypeNotSupported(e: HttpMediaTypeNotSupportedException): ResponseEntity<ApiError> {
    log.error("Exception due to invalid or missing media type header in api request: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST.value())
      .body(
        ApiError(
          error = ApiErrorTypes.INVALID_REQUEST.toString(),
          errorDescription = "${e.message}",
        ),
      )
  }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleQueryParamValidationException(e: MissingServletRequestParameterException): ResponseEntity<ApiError> {
    log.error("MissingServletRequestParameterException due to invalid request param {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST.value())
      .body(
        ApiError(
          error = ApiErrorTypes.INVALID_REQUEST.toString(),
          errorDescription = "Invalid value passed in ${e.parameterName} for ${e.parameterType}",
        ),
      )
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun handleHttpMethodNotSupported(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiError> {
    log.error("HttpRequestMethodNotSupportedException due to invalid HTTP method {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST.value())
      .body(
        ApiError(
          error = ApiErrorTypes.INVALID_REQUEST.toString(),
          errorDescription = "${e.message}",
        ),
      )
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ApiError?>? {
    if (e is MethodArgumentTypeMismatchException) {
      log.error("MethodArgumentTypeMismatchException due to invalid request {}", e.message)
      return ResponseEntity
        .status(HttpStatus.BAD_REQUEST.value())
        .body(
          ApiError(
            error = ApiErrorTypes.INVALID_REQUEST.toString(),
            errorDescription = "Invalid value passed in  ${e.name}, ${e.rootCause?.message}",
          ),
        )
    }
    if (e is ServletException) {
      log.error("ServletException due to invalid request {}", e.message)
      return ResponseEntity
        .status(HttpStatus.BAD_REQUEST.value())
        .body(
          ApiError(
            error = ApiErrorTypes.INVALID_REQUEST.toString(),
            errorDescription = "${e.message}",
          ),
        )
    }
    log.error("Unexpected exception {} {}", e.message, e.cause)
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
