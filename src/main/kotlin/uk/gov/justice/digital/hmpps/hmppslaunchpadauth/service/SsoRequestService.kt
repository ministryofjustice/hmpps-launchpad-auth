package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import java.time.LocalDateTime
import java.util.*

@Service
class SsoRequestService(private var ssoRequestRepository: SsoRequestRepository,
                         private var clientService: ClientService) {
  fun createSsoRequest(ssoRequest: SsoRequest): SsoRequest {
    return ssoRequestRepository.save(ssoRequest)
  }

  fun updateSsoRequest(ssoRequest: SsoRequest): SsoRequest {
    return ssoRequestRepository.save(ssoRequest)
  }

  fun getSsoRequestById(id: UUID): Optional<SsoRequest> {
    return ssoRequestRepository.findById(id)
  }

  fun generateSsoRequest(
    scopes: Set<Scope>,
    state: String?,
    nonce: String?,
    redirectUri: String,
    clientId: UUID,
  ): UUID {
    val ssoRequest = SsoRequest(
      UUID.randomUUID(),
      nonce,
      LocalDateTime.now(),
      null,
      SsoClient(
        clientId,
        state,
        nonce,
        scopes,
        redirectUri,
      ),
      null
    )
    return createSsoRequest(ssoRequest).id
  }

  fun updateSsoRequestAuthCodeAndUserId(userId: String, state: UUID) : String {
    val ssoRequest = ssoRequestRepository.findById(state)
    if (ssoRequest.isPresent) {
      val code = UUID.randomUUID()
      val record = ssoRequest.get()
      if (record.authorizationCode == null ) {
        record.authorizationCode = code
      } else {
        throw ApiException("Resubmittion not allowed", 400)
      }
      record.userId = userId
      updateSsoRequest(record)
      return "${record.client.reDirectUri}?code=$code&state=${record.client.state}"
    } else {
      throw ApiException("Access Denied", 403)
    }
  }

  fun getSsoRequestScopes(id: UUID): Set<Scope> {
    val ssoRequest = ssoRequestRepository.findById(id)
    if (ssoRequest.isPresent) {
      return ssoRequest.get().client.scopes
    } else {
      throw ApiException("SsoRequest not found", 400)
    }
  }

  fun getClient(id: UUID): Client {
    val ssoRequest = getSsoRequestById(id)
    if (ssoRequest.isPresent) {
      val clientId = ssoRequest.get().client.id
      val client = clientService.getClientById(clientId)
      if (client.isPresent) {
        return client.get()
      } else {
        throw ApiException("Client not found", 400)
      }
    } else {
      throw ApiException("SsoRequest not found", 400)
    }
  }

}