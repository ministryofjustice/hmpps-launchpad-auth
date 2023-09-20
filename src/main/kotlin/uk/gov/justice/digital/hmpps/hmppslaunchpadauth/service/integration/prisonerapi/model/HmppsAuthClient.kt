package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.Base64

@Component
class HmppsAuthClient {

  @Value("\${hmpps.auth.url}")
  private lateinit var hmppsAuthBaseUrl: String

  @Value("\${hmpps.auth.username}")
  private lateinit var hmppsAuthUsername: String

  @Value("\${hmpps.auth.password}")
  private lateinit var hmppsAuthPassword: String
  fun getAccessToken(): String {
    val headers = LinkedMultiValueMap<String, String>()
    headers.add("Authorization", getAuthHeader())
    headers.add("Content-Type", "application/json")
    val response = RestTemplate().exchange(
      RequestEntity<Any>(headers, HttpMethod.POST, URI("$hmppsAuthBaseUrl/auth/oauth/token?grant_type=client_credentials")),
      object : ParameterizedTypeReference<HmppsAuthAccessToken>() {},
    )
    return "Bearer ${response.body.accessToken}"
  }

  private fun getAuthHeader() : String {
    val decoder  = Base64.getDecoder()
    val username = String(decoder.decode(hmppsAuthUsername))
    val password = String(decoder.decode(hmppsAuthPassword))
    val encoder = Base64.getEncoder()
    val authCode = String(encoder.encode("$username:$password".toByteArray(Charsets.UTF_8)))
    return "Basic $authCode"
  }
}