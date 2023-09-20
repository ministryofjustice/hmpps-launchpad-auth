package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.net.URI

@Component
class PrisonApiClient(
  private var hmppsAuthClient: HmppsAuthClient,
  @Qualifier("establishments")
  private var prisonEstablisments: PrisonEstablisments,
) {

  @Value("\${hmpps.prison.url}")
  private lateinit var hmppsPrisonApiBaseUrl: String

  fun getPrisonerProfileToken(offenderId: String): PrisonerProfile {
    val accessToken = hmppsAuthClient.getAccessToken()
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", accessToken)
    headers.add("Content-Type", "application/json")
    val response = RestTemplate().exchange(
      RequestEntity<Any>(
        headers,
        HttpMethod.GET,
        URI("$hmppsPrisonApiBaseUrl/api/bookings/offenderNo/$offenderId?fullInfo=false&extraInfo=false&csraSummary=false"),
      ),
      object : ParameterizedTypeReference<PrisonerProfile>() {},
    )
    return response.body
  }
}