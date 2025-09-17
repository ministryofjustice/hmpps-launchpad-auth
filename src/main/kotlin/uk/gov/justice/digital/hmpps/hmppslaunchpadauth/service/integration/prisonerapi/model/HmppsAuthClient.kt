package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.HMPPS_AUTH_ACCESS_TOKEN_CACHE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.util.*

@Component
class HmppsAuthClient(private var webClientBuilder: WebClient.Builder) {

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
    logger.info("Calling HMPPS Auth service for access token")
    try {
      val webClient = webClientBuilder
        .baseUrl(hmppsAuthBaseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, getBasicAuthHeader())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()
      val response = webClient.post()
        .uri("/oauth/token?grant_type=client_credentials")
        .retrieve()
        .toEntity(HmppsAuthAccessToken::class.java)
        .block()!!

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
    } catch (e: WebClientResponseException) {
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
