package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.util.*

@Component
class IdTokenProcessor(private var userIdValidator: UserIdValidator) : TokenProcessor {
  companion object {
    private val logger = LoggerFactory.getLogger(IdTokenProcessor::class.java)
  }

  override fun getUserId(token: String, nonce: String): String {
    logger.debug("Id token received: {}", token)
    val decoder = Base64.getDecoder()
    val chunks = token.split(".")
    val payload = String(decoder.decode(chunks[1]))
    val nonceInIdToken = getClaimFromPayload(payload, "nonce")
    validateNonce(nonceInIdToken, nonce)
    val userId = getClaimFromPayload(payload, "email")
    if (userId != null) {
      if (!userIdValidator.isValid(userId)) {
        logger.warn("Potentially invalid user id: {}", userId)
      }
      logger.info("Logged user id : {}", userId)
      return userId
    } else {
      val message = "User id not found in payload"
      logger.error(message)
      throw IllegalArgumentException(message)
      //throw ApiException(ACCESS_DENIED, HttpStatus.BAD_REQUEST.value(), ApiErrorTypes.ACCESS_DENIED.toString(), ACCESS_DENIED)
    }
  }

  private fun validateNonce(nonceToken: String, nonceSsoRequest: String) {
    if (nonceToken != nonceSsoRequest) {
      val message = "Nonce in sso request not matching with nonce in id token payload"
      logger.error(message)
      throw IllegalArgumentException(message)
      //throw ApiException(ACCESS_DENIED, HttpStatus.BAD_REQUEST.value(), ApiErrorTypes.ACCESS_DENIED.toString(), ACCESS_DENIED)
    }
  }

  private fun getClaimFromPayload(payload: String, claimName: String): String {
    try {
      val jsonObject = JSONObject(payload)
      return jsonObject.getString(claimName)
    } catch (exception: JSONException) {
      logger.error("Claim: {} not found in id token payload", claimName)
      val message = String.format("Claim: %s not found", claimName)
      throw ApiException(message, 500, ApiErrorTypes.SERVER_ERROR.toString(), "Internal server error")
    }
  }
}
