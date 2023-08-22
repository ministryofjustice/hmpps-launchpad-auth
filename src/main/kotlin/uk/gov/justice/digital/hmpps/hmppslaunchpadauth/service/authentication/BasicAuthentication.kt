package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.UNAUTHORIZED_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import java.util.*


@Service("basicAuthentication")
class BasicAuthentication(
  private var clientService: ClientService,
  private var passwordEncoder: BCryptPasswordEncoder,
) : Authentication {
  companion object {
    private const val BASIC = "Basic "
    private const val CHUNKS_SIZE = 2
    private val logger = LoggerFactory.getLogger(BasicAuthentication::class.java)
  }


  override fun authenticate(credential: String): AuthenticationInfo {
    if (credential.startsWith(BASIC)) {
      val token = credential.replace(BASIC, "")
      val decoder = Base64.getDecoder()
      val credentialInfo = String(decoder.decode(token))
      validateCredentialFormat(credentialInfo)
      val chunks = credentialInfo.split(":")
      validateChunksSize(chunks.size)
      val clientId = chunks[0]
      val clientSecret = chunks[1]
      val client = clientService.getClientById(UUID.fromString(clientId)).orElseThrow {
        val message = String.format("Client record with id %s do not exist", clientId)
        throw ApiException(message, HttpStatus.UNAUTHORIZED.value(), ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED_MSG)
      }
      if (!client.enabled) {
        val message = String.format("Client record with id %s is not enabled", clientId)
        throw ApiException(message, HttpStatus.UNAUTHORIZED.value(), ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED_MSG)
      }
      if (passwordEncoder.matches(clientSecret, client.secret)) {
        logger.info("Successful basic auth login for client id {}", clientId)
        return AuthenticationInfo(client.id)
      } else {
        throw ApiException(UNAUTHORIZED_MSG, HttpStatus.UNAUTHORIZED.value(), ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED_MSG)
      }
    } else {
      val message = "Invalid basic authorisation header format"
      throw ApiException(message, HttpStatus.UNAUTHORIZED.value(), ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED_MSG)
    }
  }

  private fun validateCredentialFormat(credentialInfo: String) {
    if (!credentialInfo.contains(":")) {
      val message = "Invalid auth header basic format"
      throw ApiException(message, HttpStatus.UNAUTHORIZED.value(), ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED_MSG)
    }
  }

  private fun validateChunksSize(size: Int) {
    if (size != CHUNKS_SIZE) {
      val message = "Invalid auth header basic format"
      throw ApiException(message, HttpStatus.UNAUTHORIZED.value(), ApiErrorTypes.UNAUTHORIZED.toString(), UNAUTHORIZED_MSG)
    }
  }
}
