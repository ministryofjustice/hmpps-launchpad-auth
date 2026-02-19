package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.analytics.AppInsightEventType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.analytics.TelemetryService
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.HMPPS_AUTH_ACCESS_TOKEN_CACHE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INTERNAL_SERVER_ERROR_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_PRISONER_ID
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.util.*

@Component
class PrisonApiClient(
  private var hmppsAuthClient: HmppsAuthClient,
  private var cacheManager: JCacheCacheManager,
  private val webClientBuilder: WebClient.Builder,
  private var telemetryService: TelemetryService,
) {

  @Value("\${hmpps.prison-api.url}")
  private lateinit var hmppsPrisonApiBaseUrl: String

  companion object {
    private val logger = LoggerFactory.getLogger(PrisonApiClient::class.java)
  }

  fun getOffenderBooking(offenderId: String, clientId: UUID): OffenderBooking {
    val accessToken = hmppsAuthClient.getBearerToken()
    try {
      val response = connectToPrisonApi(accessToken, offenderId, clientId)
      return handlePrisonApiResponse(offenderId, response)
    } catch (e: WebClientResponseException) {
      var message: String
      if (e.statusCode.value() == HttpStatus.UNAUTHORIZED.value()) {
        cacheManager.getCache(HMPPS_AUTH_ACCESS_TOKEN_CACHE)?.clear()
        message = "Invalid or Expired access token sent to Prison API"
      } else if (e.statusCode.value() == HttpStatus.NOT_FOUND.value()) {
        message = "Record for offender id: $offenderId  do not exist"

        telemetryService.addTelemetryData(
          AppInsightEventType.LOGIN_SUCCESSFUL_BUT_PRISONER_RECORD_NOT_FOUND,
          offenderId,
          clientId,
        )
        throw ApiException("$message", HttpStatus.NOT_FOUND, ApiErrorTypes.SERVER_ERROR.toString(), INVALID_PRISONER_ID)
      } else if (e.message != null) {
        message = e.message!!
      } else {
        message = "Unexpected exception when calling Prison Api"
      }
      throw ApiException("$message", HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    }
  }

  private fun handlePrisonApiResponse(offenderId: String, response: ResponseEntity<OffenderBooking>): OffenderBooking {
    if (response.statusCode.is2xxSuccessful && response.body != null && response.body as OffenderBooking != null) {
      return response.body as OffenderBooking
    } else if (response.statusCode.value() == HttpStatus.NOT_FOUND.value()) {
      val message = "Record for offender id: $offenderId  do not exist"
      throw ApiException(message, HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    } else {
      throw ApiException("Response code ${response.statusCode.value()} making request to Prison Api", HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    }
  }

  private fun connectToPrisonApi(accessToken: String, offenderId: String, clientId: UUID): ResponseEntity<OffenderBooking> {
    val webClient = webClientBuilder
      .baseUrl(hmppsPrisonApiBaseUrl)
      .defaultHeader(HttpHeaders.AUTHORIZATION, accessToken)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .build()

    logger.info("Calling Prison Api for getting profile for offender id: $offenderId")

    val response = webClient.get()
      .uri("/api/bookings/offenderNo/$offenderId?fullInfo=false&extraInfo=false&csraSummary=false")
      .retrieve()
      .toEntity(OffenderBooking::class.java)
      .block()!!
    return response
  }
}
