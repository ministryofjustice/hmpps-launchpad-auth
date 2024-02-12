package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.HMPPS_AUTH_ACCESS_TOKEN_CACHE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.net.URI
import java.util.*

@Component
class HmppsAuthClient(@Qualifier("restTemplate") private var restTemplate: RestTemplate) {

  @Value("\${hmpps.auth.url}")
  private lateinit var hmppsAuthBaseUrl: String

  @Value("\${hmpps.auth.username}")
  private lateinit var hmppsAuthUsername: String

  @Value("\${hmpps.auth.password}")
  private lateinit var hmppsAuthPassword: String

  companion object {
    private val logger = LoggerFactory.getLogger(HmppsAuthClient::class.java)
  }

  @Cacheable(HMPPS_AUTH_ACCESS_TOKEN_CACHE, key = "#root.methodName")
  fun getBearerToken(): String {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add(HttpHeaders.AUTHORIZATION, getBasicAuthHeader())
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    logger.info("Calling HMPPS Auth service for access token")
    try {
      val response = restTemplate.exchange(
        RequestEntity<Any>(
          headers,
          HttpMethod.POST,
          URI("$hmppsAuthBaseUrl/auth/oauth/token?grant_type=client_credentials"),
        ),
        object : ParameterizedTypeReference<HmppsAuthAccessToken>() {},
      )
      if (response.statusCode.is2xxSuccessful && response.body != null) {
        return "Bearer ${response.body.accessToken}"
      } else {
        throw ApiException(
          "Response code ${response.statusCode.value()} making request to Hmpps auth for access token",
          HttpStatus.INTERNAL_SERVER_ERROR,
          ApiErrorTypes.SERVER_ERROR.toString(),
          "Server Error",
        )
      }
    } catch (e: HttpClientErrorException) {
      throw ApiException(
        "Response code ${e.statusCode.value()} making request to Hmpps auth for access token",
        HttpStatus.INTERNAL_SERVER_ERROR,
        ApiErrorTypes.SERVER_ERROR.toString(),
        "Server Error",
      )
    }
  }

  private fun getBasicAuthHeader(): String {
    val encoder = Base64.getEncoder()
    val authCode = String(encoder.encode("$hmppsAuthUsername:$hmppsAuthPassword".toByteArray(Charsets.UTF_8)))
    return "Basic $authCode"
  }
}
