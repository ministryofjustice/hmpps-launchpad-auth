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

  fun getClientById(id: UUID): Optional<Client> {
    return clientRepository.findById(id)
  }

  fun validateParams(
    clientId: UUID,
    responseType: String,
    scopes: String,
    redirectUri: String,
    state: String?,
    nonce: String?,
  ) {
    val client = clientRepository.findById(clientId).orElseThrow {
      logger.error("Client with client_id {} does not exist", clientId)
      ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
    logger.info("Initiate user sign in process for client id: {}", client.id)
    isEnabled(client.enabled)
    validateScopes(scopes, client.scopes)
    validateAuthorizationGrantType(responseType, client.authorizedGrantTypes)
    validateUri(redirectUri, client.registeredRedirectUris)
  }

  private fun isEnabled(enabled: Boolean) {
    if (!enabled) {
      logger.debug("Client not enabled")
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }

  private fun validateUri(uri: String, redirectUris: Set<String>) {
    try {
      URL(uri)
      validateRedirectUri(uri, redirectUris)
    } catch (exception: MalformedURLException) {
      logger.error("Not a valid redirect url: {}", uri)
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
        logger.debug(String.format("Scope %s not matching with client scope set", x))
        throw ApiException(IN_VALID_SCOPE, BAD_REQUEST_CODE)
      }
    }
  }

  private fun validateAuthorizationGrantType(grant: String, clientGrants: Set<AuthorizationGrantType>) {
    if (!AuthorizationGrantType.isStringMatchEnumValue(grant, clientGrants)) {
      logger.debug("Authorization grant type {} not matching with client grants", grant)
      throw ApiException(IN_VALID_GRANT, BAD_REQUEST_CODE)
    }
  }

  private fun validateRedirectUri(uri: String, redirectUris: Set<String>) {
    if (!redirectUris.contains(uri)) {
      logger.debug("Redirect uri not matching with client redirect uri: {}", uri)
      throw ApiException(IN_VALID_REDIRECT_URI, BAD_REQUEST_CODE)
    }
  }
}
