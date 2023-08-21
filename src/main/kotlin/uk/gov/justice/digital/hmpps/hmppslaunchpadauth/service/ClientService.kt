package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.ACCESS_DENIED
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.ACCESS_DENIED_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.BAD_REQUEST_CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.CODE
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.IN_VALID_GRANT_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.IN_VALID_REDIRECT_URI_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.IN_VALID_SCOPE_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import java.net.MalformedURLException
import java.net.URL
import java.util.*


@Service
class ClientService(private var clientRepository: ClientRepository) {
  companion object {
    private val logger = LoggerFactory.getLogger(ClientService::class.java)
  }

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
      SsoException(ACCESS_DENIED, ACCESS_DENIED_CODE, "invalid_clientId", "Invalid client id", redirectUri)
    }
    logger.info("Initiate user sign in process for client id: {}", client.id)
    isEnabled(client.enabled, redirectUri)
    validateScopes(scopes, client.scopes, redirectUri)
    validateAuthorizationGrantType(responseType, redirectUri)
    validateUri(redirectUri, client.registeredRedirectUris)
    logger.info(
      "Single sign on request received for client: {}, with response_type: {}, scopes: {}, redirect_uri: {}",
      clientId,
      responseType,
      scopes,
      redirectUri
      )
  }

  private fun isEnabled(enabled: Boolean, redirectUri: String) {
    if (!enabled) {
      logger.debug("Client not enabled")
      throw SsoException(ACCESS_DENIED, ACCESS_DENIED_CODE, "invalid_client", "Client not found or invalid id format", redirectUri)
    }
  }

  private fun validateUri(uri: String, redirectUris: Set<String>) {
    try {
      URL(uri)
      validateRedirectUri(uri, redirectUris)
    } catch (exception: MalformedURLException) {
      logger.error("Not a valid redirect url: {}", uri)
      throw SsoException(IN_VALID_REDIRECT_URI_DESCRIPTION, BAD_REQUEST_CODE, "invalid_redirectUri", "redirect url invalid or not listed", uri)
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
        logger.debug("Scope {} not matching with client scope set", x)
        throw SsoException(IN_VALID_SCOPE_DESCRIPTION, BAD_REQUEST_CODE, "invalid_scope", "Invalid scope or not listed", redirectUri)
      }
    }
  }

  private fun validateAuthorizationGrantType(grant: String, redirectUri: String) {
    if (grant != CODE) {
      logger.debug("Authorization grant type {} not matching with client grants", grant)
      throw SsoException(IN_VALID_GRANT_DESCRIPTION, BAD_REQUEST_CODE, "invalid_grant", "Grant type invalid or not allowed", redirectUri)
    }
  }

  private fun validateRedirectUri(uri: String, redirectUris: Set<String>) {
    if (!redirectUris.contains(uri)) {
      logger.debug("Redirect uri not matching with client redirect uri: {}", uri)
      throw SsoException(IN_VALID_REDIRECT_URI_DESCRIPTION, BAD_REQUEST_CODE, "invalid_redirectUri", "Invalid redirect uri or not listed", uri)
    }
  }
}
