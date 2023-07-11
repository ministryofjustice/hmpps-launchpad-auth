package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.util.*

@Component
class IdTokenProcessor : TokenProcessor {
  private val logger = LoggerFactory.getLogger(IdTokenProcessor::class.java)

  override fun getUserId(token: String, nonce: String): String {
    logger.debug(String.format("Id token received: %s", token))
    val decoder = Base64.getUrlDecoder()
    val chunks = token.split(".")
    val payload = String(decoder.decode(chunks[1]))
    val nonceInIdToken = getClaimFromPayload(payload,"nonce")
    validateNonce(nonceInIdToken, nonce)
    val userId = getClaimFromPayload(payload,"email")
    if (userId != null) {
      logger.info(String.format("Logged user id : %s", userId))
      return userId
    } else {
      logger.error("User id not found in payload")
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun validateNonce(nonceToken: String, nonceSsoRequest: String) {
    if (nonceToken != nonceSsoRequest) {
      logger.error("Nonce in sso request not matching with nonce in id token payload")
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun getClaimFromPayload(payload: String, claimName: String) : String {
    try {
      val jsonObject = JSONObject(payload)
      return jsonObject.getString(claimName)
    } catch(exception: JSONException) {
      logger.error(String.format("Claim: %s not found in id token payload", claimName))
      throw ApiException(String.format("Claim: %s not found", claimName), 500)
    }
  }
}
