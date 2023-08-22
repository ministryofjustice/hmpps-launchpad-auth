package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_RESPONSE_TYPE_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.REDIRECTION_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import java.net.MalformedURLException
import java.net.URL
import java.util.*


@Service
class ClientService(private var clientRepository: ClientRepository) {

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
      val message = String.format("Client with client_id %s does not exist", clientId)
      SsoException(message, REDIRECTION_CODE, "invalid_clientId", "Invalid client id", redirectUri)
    }
    isEnabled(client.enabled, redirectUri)
    validateScopes(scopes, client.scopes, redirectUri)
    validateResponseType(responseType, redirectUri)
    validateUri(redirectUri, client.registeredRedirectUris)
  }

  private fun isEnabled(enabled: Boolean, redirectUri: String) {
    if (!enabled) {
      throw SsoException("Client not enabled", REDIRECTION_CODE, ApiErrorTypes.INVALID_CLIENT.toString(), "Client is disabled", redirectUri)
    }
  }

  private fun validateUri(uri: String, redirectUris: Set<String>) {
    try {
      URL(uri)
      validateRedirectUri(uri, redirectUris)
    } catch (exception: MalformedURLException) {
      val message = String.format("Not a valid redirect url: %s", uri)
      throw SsoException(message, REDIRECTION_CODE, ApiErrorTypes.INVALID_REDIRECT_URI.toString(), "Redirect url invalid or not listed", uri)
    }
  }

  private fun validateScopes(scopes: String, clientScopes: Set<Scope>, redirectUri: String) {
    val scopeList: List<String>
    if (scopes.contains(" ")) {
      val scopeValues = scopes.replace("\\s+".toRegex(), " ")
      scopeList = scopeValues.split("\\s".toRegex())
    } else {
      scopeList = listOf(scopes)
    }
    scopeList.forEach { x ->
      if (!Scope.isStringMatchEnumValue(x, clientScopes)) {
        val message = String.format("Scope %s not matching with client scope set", x)
        throw SsoException(message, REDIRECTION_CODE, ApiErrorTypes.INVALID_SCOPE.toString(), "Invalid scope", redirectUri)
      }
    }
  }

  private fun validateResponseType(responseType: String, redirectUri: String) {
    if (responseType != CODE) {
      val message = String.format("Invalid response type %s send in sso  request", responseType)
      throw SsoException(message, REDIRECTION_CODE, ApiErrorTypes.INVALID_RESPONSE_TYPE.toString(), INVALID_RESPONSE_TYPE_MSG, redirectUri)
    }
  }

  private fun validateRedirectUri(uri: String, redirectUris: Set<String>) {
    if (!redirectUris.contains(uri)) {
      val message = String.format("Redirect uri not matching with client redirect uri: %s", uri)
      throw SsoException(message, REDIRECTION_CODE, ApiErrorTypes.INVALID_REDIRECT_URI.toString(), "Invalid redirect uri or not listed", uri)
    }
  }
}
