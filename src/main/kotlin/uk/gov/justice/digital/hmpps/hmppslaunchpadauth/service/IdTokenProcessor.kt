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

  override fun getUserId(token: String, nonce: String): String {
    val decoder = Base64.getUrlDecoder()
    val chunks = token.split(".")
    val payload = String(decoder.decode(chunks[1]))
    println(payload)
    val jsonObject = JSONObject(payload)
    validateClient(jsonObject.get("aud") as String)
    validateExpirationTime(jsonObject.get("exp") as Int)
    validateNonce(jsonObject.get("nonce") as String, nonce)
    val userId = jsonObject.get("preferred_username") as String?
    if (userId != null) {
      return userId
    } else {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun validateClient(clientId: String) {
    if (!clientId.equals(launchpadClientId)) {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun validateNonce(nonceToken: String, nonceSsoRequest: String) {
    if (nonceToken != nonceSsoRequest) {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun validateExpirationTime(time: Int) {
    val instant = Instant.now()
    val now = instant.epochSecond
    if (now > time) {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }
}
