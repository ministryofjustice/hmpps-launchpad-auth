package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.time.Instant
import java.util.*

@Component
class IdTokenProcessor : TokenProcessor {
  @Value("\${azure.oauth2-url}")
  private lateinit var azureOauthUrl: String

  @Value("\${azure.client-id}")
  private lateinit var launchpadClientId: String

  @Value("\${azure.launchpad-redirectUri}")
  private lateinit var launchpadRedirectUrl: String

  @Value("\${azure.issuer-url}")
  private lateinit var issuerUrl: String

  override fun getUserId(token: String): String {
    val decoder = Base64.getUrlDecoder()
    val chunks = token.split(".")
    val payload = String(decoder.decode(chunks[1]))
    println(payload)
    val jsonObject = JSONObject(payload)
    validateClient(jsonObject.get("aud") as String)
    validateExpirationTime(jsonObject.get("exp") as Int)
    return jsonObject.get("preferred_username") as String
  }

  private fun validateClient(clientId: String) {
    if (!clientId.equals(launchpadClientId)) {
      throw ApiException("Access Denied, invalid client", 403)
    }
  }

  private fun validateExpirationTime(time: Int) {
    val instant = Instant.now()
    val now  = instant.epochSecond
    if (now > time) {
      throw ApiException("Access Denied, expired token", 403)
    }
  }

}