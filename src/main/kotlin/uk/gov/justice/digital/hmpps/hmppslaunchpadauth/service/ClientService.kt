package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_CLIENT_ID_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_REDIRECT_URI_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_RESPONSE_TYPE_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INVALID_SCOPE_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
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
      val message = "Client with client_id $clientId does not exist"
      ApiException(message, HttpStatus.FORBIDDEN, ApiErrorTypes.ACCESS_DENIED.toString(), INVALID_CLIENT_ID_MSG)
    }
    isEnabled(client.enabled, redirectUri, state)
    validateScopes(scopes, client.scopes, redirectUri, state)
    validateResponseType(responseType, redirectUri, state)
    validateUri(redirectUri, client.registeredRedirectUris, state)
  }

  private fun isEnabled(enabled: Boolean, redirectUri: String, state: String?) {
    if (!enabled) {
      throw SsoException(
        "Client not enabled",
        HttpStatus.FOUND,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        INVALID_CLIENT_ID_MSG,
        redirectUri,
        state,
      )
    }
  }

  private fun validateUri(redirectUri: String, redirectUris: Set<String>, state: String?) {
    try {
      URL(redirectUri)
      validateRedirectUri(redirectUri, redirectUris, state)
    } catch (exception: MalformedURLException) {
      val message = "Not a valid redirect uri: $redirectUri"
      throw ApiException(
        message,
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.ACCESS_DENIED.toString(),
        INVALID_REDIRECT_URI_MSG,
      )
    }
  }

  private fun validateScopes(scopes: String, clientScopes: Set<Scope>, redirectUri: String, state: String?) {
    val scopeList: List<String>
    if (scopes.contains(" ")) {
      val scopeValues = scopes.replace("\\s+".toRegex(), " ")
      scopeList = scopeValues.split("\\s".toRegex())
    } else {
      scopeList = listOf(scopes)
    }
    scopeList.forEach { x ->
      if (!Scope.isStringMatchEnumValue(x, clientScopes)) {
        val message = "Scope $x not matching with client scope set"
        throw SsoException(
          message,
          HttpStatus.FOUND,
          ApiErrorTypes.INVALID_SCOPE.toString(),
          INVALID_SCOPE_MSG,
          redirectUri,
          state,
        )
      }
    }
  }

  private fun validateResponseType(responseType: String, redirectUri: String, state: String?) {
    if (responseType != CODE) {
      val message = "Invalid response type $responseType send in sso  request"
      throw SsoException(
        message,
        HttpStatus.FOUND,
        ApiErrorTypes.INVALID_REQUEST.toString(),
        INVALID_RESPONSE_TYPE_MSG,
        redirectUri,
        state,
      )
    }
  }

  private fun validateRedirectUri(redirectUri: String, redirectUris: Set<String>, state: String?) {
    if (!redirectUris.contains(redirectUri)) {
      val message = "Redirect uri not matching with client redirect uri: $redirectUri"
      throw ApiException(
        message,
        HttpStatus.FORBIDDEN,
        ApiErrorTypes.ACCESS_DENIED.toString(),
        INVALID_REDIRECT_URI_MSG,
      )
    }
  }
}
