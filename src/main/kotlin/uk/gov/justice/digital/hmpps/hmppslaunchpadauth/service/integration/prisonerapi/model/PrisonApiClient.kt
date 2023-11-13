package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.net.URI

@Component
class PrisonApiClient(
  private var hmppsAuthClient: HmppsAuthClient,
  @Qualifier("restTemplate") private var restTemplate: RestTemplate,
) {

  @Value("\${hmpps.prison.url}")
  private lateinit var hmppsPrisonApiBaseUrl: String

  companion object {
    private val logger = LoggerFactory.getLogger(PrisonApiClient::class.java)
  }

  fun getPrisonerProfileToken(offenderId: String): PrisonerProfile {
    val accessToken = hmppsAuthClient.getAccessToken()
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", accessToken)
    headers.add("Content-Type", "application/json")
    logger.debug("Calling Prison Api for getting profile for offender id: $offenderId")
    val response = restTemplate.exchange(
      RequestEntity<Any>(
        headers,
        HttpMethod.GET,
        URI("$hmppsPrisonApiBaseUrl/api/bookings/offenderNo/$offenderId?fullInfo=false&extraInfo=false&csraSummary=false"),
      ),
      object : ParameterizedTypeReference<PrisonerProfile>() {},
    )
    if (response.statusCode.is2xxSuccessful) {
      logger.info("Profile received in response from Prison Api for offender id: $offenderId")
      return response.body!!
    } else {
      throw ApiException("Response code ${response.statusCode.value()} making request to Prison Api", HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), "Server Error")
    }
  }
}
