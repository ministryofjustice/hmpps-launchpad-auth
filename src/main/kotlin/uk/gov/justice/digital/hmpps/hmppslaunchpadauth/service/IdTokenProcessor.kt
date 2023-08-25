package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INTERNAL_SERVER_ERROR_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.util.*

@Component
class IdTokenProcessor(private var userIdValidator: UserIdValidator) : TokenProcessor {
  companion object {
    private val logger = LoggerFactory.getLogger(IdTokenProcessor::class.java)
  }

  override fun getUserId(token: String, nonce: String): String {
    logger.debug("Id token received from azure ad: {}", token)
    val decoder = Base64.getDecoder()
    val chunks = token.split(".")
    val payload = String(decoder.decode(chunks[1]))
    val nonceInIdToken = getClaimFromPayload(payload, "nonce")
    validateNonce(nonceInIdToken, nonce)
    // The claim containing user id will be checked again after integrating with prison api
    val userId = getClaimFromPayload(payload, "email")
    if (userId != null) {
      if (!userIdValidator.isValid(userId)) {
        logger.warn("Potentially invalid user id: {}", userId)
      }
      logger.info("Logged user id : {}", userId)
      return userId
    } else {
      val message = "User id not found in payload"
      throw IllegalArgumentException(message)
    }
  }

  private fun validateNonce(nonceToken: String, nonceSsoRequest: String) {
    if (nonceToken != nonceSsoRequest) {
      val message = "Nonce in sso request not matching with nonce in id token payload"
      throw IllegalArgumentException(message)
    }
  }

  private fun getClaimFromPayload(payload: String, claimName: String): String {
    try {
      val jsonObject = JSONObject(payload)
      return jsonObject.getString(claimName)
    } catch (exception: JSONException) {
      val message = "Claim: $claimName not found"
      throw ApiException(message, HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    }
  }
}
