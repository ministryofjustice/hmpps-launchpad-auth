package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.AuthorizationGrantType
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import java.net.MalformedURLException
import java.net.URL
import java.util.*

const val ACCESS_DENIED = "Access denied"
const val IN_VALID_SCOPE = "The requested scope is invalid or not found."
const val IN_VALID_GRANT = "The requested response type is invalid or not found."
const val IN_VALID_REDIRECT_URI = "The requested redirect uri is invalid or not found"
const val BAD_REQUEST_CODE = 400
const val ACCESS_DENIED_CODE = 403

@Service
class ClientService(private var clientRepository: ClientRepository) {
  private val logger = LoggerFactory.getLogger(ClientService::class.java)

  fun validateParams(
    clientId: UUID,
    responseType: String,
    scopes: String,
    redirectUri: String,
    state: String?,
    nonce: String?,
  ) {
    val client: Optional<Client> = clientRepository.findById(clientId)
    if (client.isEmpty) {
      val message = String.format("Client with client_id %s does not exist", clientId)
      logger.info(message)
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    } else {
      val clientRecord = client.get()
      isEnabled(clientRecord.enabled)
      validateScopes(scopes, clientRecord.scopes)
      validateAuthorizationGrantType(responseType, clientRecord.authorizedGrantTypes)
      validateUri(redirectUri, clientRecord.registeredRedirectUris)
    }
  }

  private fun isEnabled(enabled: Boolean) {
    if (!enabled) {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }
  private fun validateUri(uri: String, redirectUris: Set<String>) {
    try {
      URL(uri)
      validateRedirectUri(uri, redirectUris)
    } catch (exception: MalformedURLException) {
      throw ApiException(IN_VALID_REDIRECT_URI, BAD_REQUEST_CODE)
    }
  }
  private fun validateScopes(scopes: String, clientScopes: Set<Scope>) {
    var scopeList: List<String>
    if (scopes.contains(" ")) {
      val scopeValues = scopes.replace("\\s+".toRegex(), " ")
      scopeList = scopeValues.split("\\s".toRegex())
    } else {
      scopeList = listOf(scopes)
    }
    scopeList.forEach { x ->
      if (!Scope.isStringMatchEnumValue(x, clientScopes)) {
        throw ApiException(IN_VALID_SCOPE, BAD_REQUEST_CODE)
      }
    }
  }

  private fun validateAuthorizationGrantType(grant: String, clientGrants: Set<AuthorizationGrantType>) {
    if (!AuthorizationGrantType.isStringMatchEnumValue(grant, clientGrants)) {
      throw ApiException(IN_VALID_GRANT, BAD_REQUEST_CODE)
    }
  }
  private fun validateRedirectUri(uri: String, redirectUris: Set<String>) {
    if (!redirectUris.contains(uri)) {
      throw ApiException(IN_VALID_REDIRECT_URI, BAD_REQUEST_CODE)
    }
  }
}
