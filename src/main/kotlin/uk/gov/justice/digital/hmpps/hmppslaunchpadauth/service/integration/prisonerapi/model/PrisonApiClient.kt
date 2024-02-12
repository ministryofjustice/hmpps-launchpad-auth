package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.HMPPS_AUTH_ACCESS_TOKEN_CACHE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INTERNAL_SERVER_ERROR_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.net.URI

@Component
class PrisonApiClient(
  private var hmppsAuthClient: HmppsAuthClient,
  @Qualifier("restTemplate") private var restTemplate: RestTemplate,
  private var cacheManager: JCacheCacheManager,
) {

  @Value("\${hmpps.prison-api.url}")
  private lateinit var hmppsPrisonApiBaseUrl: String

  companion object {
    private val logger = LoggerFactory.getLogger(PrisonApiClient::class.java)
  }

  fun getOffenderBooking(offenderId: String): OffenderBooking {
    val accessToken = hmppsAuthClient.getBearerToken()
    try {
      val response = connectToPrisonApi(accessToken, offenderId)
      return handlePrisonApiResponse(offenderId, response)
    } catch (e: HttpClientErrorException) {
      var message: String
      if (e.statusCode.value() == HttpStatus.UNAUTHORIZED.value()) {
        cacheManager.getCache(HMPPS_AUTH_ACCESS_TOKEN_CACHE).clear()
        message = "Invalid or Expired access token sent to Prison API"
      } else if (e.statusCode.value() == HttpStatus.NOT_FOUND.value()) {
        message = "Record for offender id: $offenderId  do not exist"
      } else if (e.message != null) {
        message = e.message!!
      } else {
        message = "Unexpected exception when calling Prison Api"
      }
      throw ApiException("$message", HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    }
  }

  private fun handlePrisonApiResponse(offenderId: String, response: ResponseEntity<OffenderBooking>): OffenderBooking {
    if (response.statusCode.is2xxSuccessful && response.body != null) {
      return response.body
    } else if (response.statusCode.value() == HttpStatus.NOT_FOUND.value()) {
      val message = "Record for offender id: $offenderId  do not exist"
      throw ApiException(message, HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    } else {
      throw ApiException("Response code ${response.statusCode.value()} making request to Prison Api", HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    }
  }

  private fun connectToPrisonApi(accessToken: String, offenderId: String): ResponseEntity<OffenderBooking> {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add(HttpHeaders.AUTHORIZATION, accessToken)
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    logger.info("Calling Prison Api for getting profile for offender id: $offenderId")
    return restTemplate.exchange(
      RequestEntity<Any>(
        headers,
        HttpMethod.GET,
        URI("$hmppsPrisonApiBaseUrl/api/bookings/offenderNo/$offenderId?fullInfo=false&extraInfo=false&csraSummary=false"),
      ),
      object : ParameterizedTypeReference<OffenderBooking>() {},
    )
  }
}
