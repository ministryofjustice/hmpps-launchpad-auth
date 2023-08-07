package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.ClientService
import java.util.*

const val BASIC = "Basic "
const val UNAUTHORIZED = "Unauthorized"
const val UNAUTHORIZED_CODE = 401
@Component("basicAuthentication")
class BasicAuthentication(
  private var clientService: ClientService,
  private var encoder: BCryptPasswordEncoder
  ): Authentication {


  private val logger = LoggerFactory.getLogger(BasicAuthentication::class.java)

  override fun authenticate(credential: String): AuthenticationInfo {
    if (credential != null && credential.startsWith(BASIC)) {
      val token  = credential.replace(BASIC, "")
      val decoder = Base64.getUrlDecoder()
      val credentialInfo = String(decoder.decode(token))
      val chunks  = credentialInfo.split(":")
      val username = chunks[0]
      val password = chunks[1]
      val client = clientService.getClientById(UUID.fromString(username)).orElseThrow {
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
      if (!client.enabled) {
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
      if (encoder.matches(password, client.secret)) {
        return AuthenticationInfo(client.id, client.scopes)
      } else {
        throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
      }
    } else {
      throw ApiException(UNAUTHORIZED, UNAUTHORIZED_CODE)
    }
  }
}
