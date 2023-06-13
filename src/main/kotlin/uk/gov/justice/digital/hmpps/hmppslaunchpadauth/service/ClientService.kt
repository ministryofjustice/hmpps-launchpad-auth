package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import java.net.MalformedURLException
import java.net.URL
import java.util.*

@Service
class ClientService(@Autowired var clientRepository: ClientRepository) {
  private val logger = LoggerFactory.getLogger(ClientService::class.java)

  fun validateParams(
    clientId: UUID,
    responseType: String,
    scope: String,
    redirectUri: String,
    state: String,
    nonce: String,
  ) {
    val client: Optional<Client> = clientRepository.findById(clientId)
    if (client.isEmpty) {
      val message = String.format("Client with client_id %s do not exist", clientId)
      logger.info(message)
      throw ApiException(message)
    } else {
      validateScopes(scope, client.get().scopes)
      validateUri(redirectUri, client.get().registeredRedirectUris)
    }
  }
  private fun validateUri(uri: String, redirectUris: Set<String>) {
    try {
      URL(uri)
      validateRedirectUri(uri, redirectUris)
    } catch (exception: MalformedURLException) {
      throw ApiException(String.format("Not a valid uri %s", uri))
    }
  }

  private fun validateScopes(scopes: String, clientScopeSet: Set<Scope>) {
    var scopeList: List<String>
    if (scopes.contains(",")) {
      scopeList = scopes.split(",")
    } else {
      scopeList = listOf(scopes)
    }
    scopeList.forEach { y ->
        if (!Scope.getStringValues().contains(y)) {
          throw ApiException(String.format("Scope %s is not valid scope", y))
        }
      }
  }

  private fun validateRedirectUri(uri: String, redirectUris: Set<String>) {
    if (!redirectUris.contains(uri)) {
      throw ApiException(String.format("uri %s is not in registered uri list", uri))
    }
  }
}
