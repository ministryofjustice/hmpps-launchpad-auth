package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import java.util.*

const val BASIC = "Basic "
const val UNAUTHORIZED = "Unauthorized"
const val UNAUTHORIZED_CODE = 401
const val CHUNKS_SIZE = 2

@Component("basicAuthentication")
class BasicAuthentication(
  private var clientService: ClientService,
  private var encoder: BCryptPasswordEncoder,
) : Authentication {
  private val logger = LoggerFactory.getLogger(BasicAuthentication::class.java)

  override fun authenticate(credential: String): AuthenticationInfo {
    if (credential.startsWith(BASIC)) {
      val token = credential.replace(BASIC, "")
      val decoder = Base64.getUrlDecoder()
      val credentialInfo = String(decoder.decode(token))
      validateCredentialFormat(credentialInfo)
      val chunks = credentialInfo.split(":")
      validateChunksSize(chunks.size, credentialInfo)
      val username = chunks[0]
      val password = chunks[1]
      val client = clientService.getClientById(UUID.fromString(username)).orElseThrow {
        logger.debug("Client record with id {} do not exist", username)
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
      if (!client.enabled) {
        logger.debug("Client record with id {} is not enabled", username)
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
      if (encoder.matches(password, client.secret)) {
        return AuthenticationInfo(client.id, client.scopes)
      } else {
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
    } else {
      logger.debug("Invalid basic authorisation header format")
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }

  private fun validateCredentialFormat(credentialInfo: String) {
    if (!credentialInfo.contains(":")) {
      logger.debug("Invalid basic format {}", credentialInfo)
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }

  private fun validateChunksSize(size: Int, credentialInfo: String) {
    if (size != CHUNKS_SIZE) {
      logger.debug("Invalid basic format {}", credentialInfo)
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }
}
