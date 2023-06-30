package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.time.Instant
import java.util.*

@Component
class IdTokenProcessor : TokenProcessor {
  override fun getUserId(token: String, nonce: String): String {
    val decoder = Base64.getUrlDecoder()
    val chunks = token.split(".")
    val payload = String(decoder.decode(chunks[1]))
    val jsonObject = JSONObject(payload)
    validateNonce(jsonObject.get("nonce") as String, nonce)
    val userId = jsonObject.get("preferred_username") as String?
    if (userId != null) {
      return userId
    } else {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun validateNonce(nonceToken: String, nonceSsoRequest: String) {
    if (nonceToken != nonceSsoRequest) {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

}
