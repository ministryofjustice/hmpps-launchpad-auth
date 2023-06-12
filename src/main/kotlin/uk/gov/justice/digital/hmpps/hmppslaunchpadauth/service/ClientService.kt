package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

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
  fun findClientById(id: UUID): Optional<Client> {
    return clientRepository.findById(id)
  }

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
      throw ApiException(String.format("Client with client_id %s do not exist", clientId))
    } else {
      val clientRecord = client.get()
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
    val scopeSet = scopes.split(",")
    scopeSet.forEach {
      if (!Scope.values().contains(Scope.valueOf(it))) {
        throw ApiException(String.format("Scope %s is not valid scope", it))
        }
    }
  }

  private fun validateRedirectUri(uri: String, redirectUris: Set<String>) {
    if (!redirectUris.contains(uri)) {
      throw ApiException(String.format("uri %s is not valid uri", uri))
    }
  }
}
