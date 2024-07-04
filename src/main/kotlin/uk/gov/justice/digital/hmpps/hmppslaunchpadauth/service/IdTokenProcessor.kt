package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INTERNAL_SERVER_ERROR_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_AZURE_AD_TENANT
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_USER_ID
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.validator.UserIdValidator
import java.util.*

@Component
class IdTokenProcessor(private var userIdValidator: UserIdValidator) : TokenProcessor {
  @Value("\${azure.tenant-id}")
  private lateinit var tenantId: String
  companion object {
    private val logger = LoggerFactory.getLogger(IdTokenProcessor::class.java)
  }

  override fun getUserId(token: String, nonce: String): String {
    logger.debug("Id token received from azure ad: {}", token)
    val decoder = Base64.getDecoder()
    val chunks = token.split(".")
    val payload = String(decoder.decode(chunks[1]))
    // validate tenant id
    val tenantId = getClaimFromPayload(payload, "tid", token)
    validateTenantId(tenantId)
    val nonceInIdToken = getClaimFromPayload(payload, "nonce", token)
    validateNonce(nonceInIdToken, nonce)
    // The claim containing user id will be checked again after integrating with prison api
    val email = getClaimFromPayload(payload, "preferred_username", token)
    if (email != null) {
      var userId = email.substringBefore("@")
      logger.info("Logged user id : {}", userId)
      userId = userId.trim()
      if (!userIdValidator.isValid(userId)) {
        logger.warn("Potentially invalid user id: {}", email)
        throw IllegalArgumentException(INVALID_USER_ID)
      }
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

  private fun getClaimFromPayload(payload: String, claimName: String, token: String): String {
    try {
      val jsonObject = JSONObject(payload)
      return jsonObject.getString(claimName)
    } catch (exception: JSONException) {
      val message = "Claim: $claimName not found in token:$token"
      throw ApiException(message, HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), INTERNAL_SERVER_ERROR_MSG)
    }
  }

  private fun validateTenantId(value: String?) {
    if (!StringUtils.hasText(value)) {
      val message = "Tenant id not found in azure id token"
      throw IllegalArgumentException(message)
    } else {
      if (value != tenantId) {
        val message = INVALID_AZURE_AD_TENANT
        throw IllegalArgumentException(message)
      }
    }
  }
}
