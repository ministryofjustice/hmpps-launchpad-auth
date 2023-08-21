package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.UNAUTHORIZED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.UNAUTHORIZED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import java.util.*



@Component("basicAuthentication")
class BasicAuthentication(
  private var clientService: ClientService,
  private var encoder: BCryptPasswordEncoder,
) : Authentication {
  companion object {
    private const val BASIC = "Basic "
    private const val CHUNKS_SIZE = 2
    private val LOGGER = LoggerFactory.getLogger(BasicAuthentication::class.java)
  }


  override fun authenticate(credential: String): AuthenticationInfo {
    if (credential.startsWith(BASIC)) {
      val token = credential.replace(BASIC, "")
      val decoder = Base64.getDecoder()
      val credentialInfo = String(decoder.decode(token))
      validateCredentialFormat(credentialInfo)
      val chunks = credentialInfo.split(":")
      validateChunksSize(chunks.size, credentialInfo)
      val clientId = chunks[0]
      val clientSecret = chunks[1]
      val client = clientService.getClientById(UUID.fromString(clientId)).orElseThrow {
        LOGGER.debug("Client record with id {} do not exist", clientId)
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED)
      }
      if (!client.enabled) {
        LOGGER.debug("Client record with id {} is not enabled", clientId)
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED)
      }
      if (encoder.matches(clientSecret, client.secret)) {
        return AuthenticationInfo(client.id)
      } else {
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED)
      }
    } else {
      LOGGER.debug("Invalid basic authorisation header format")
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED)
    }
  }

  private fun validateCredentialFormat(credentialInfo: String) {
    if (!credentialInfo.contains(":")) {
      LOGGER.debug("Invalid basic format {}", credentialInfo)
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED)
    }
  }

  private fun validateChunksSize(size: Int, credentialInfo: String) {
    if (size != CHUNKS_SIZE) {
      LOGGER.debug("Invalid basic format {}", credentialInfo)
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE, ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED)
    }
  }
}
