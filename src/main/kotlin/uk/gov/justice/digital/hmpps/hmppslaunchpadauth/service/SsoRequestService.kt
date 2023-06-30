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
class SsoRequestService(
  private var ssoRequestRepository: SsoRequestRepository,
  private var clientService: ClientService,
) {
  fun createSsoRequest(ssoRequest: SsoRequest): SsoRequest {
    return ssoRequestRepository.save(ssoRequest)
  }

  fun updateSsoRequest(ssoRequest: SsoRequest): SsoRequest {
    return ssoRequestRepository.save(ssoRequest)
  }

  fun getSsoRequestById(id: UUID): Optional<SsoRequest> {
    return ssoRequestRepository.findById(id)
  }

  fun deleteSsoRequestById(id: UUID) {
    ssoRequestRepository.deleteById(id)
  }

  fun generateSsoRequest(
    scopes: Set<Scope>,
    state: String?,
    nonce: String?,
    redirectUri: String,
    clientId: UUID,
  ): SsoRequest {
    val ssoRequest = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID().toString(),
      LocalDateTime.now(),
      null,
      SsoClient(
        clientId,
        state,
        nonce,
        scopes,
        redirectUri,
      ),
      null,
    )
    return createSsoRequest(ssoRequest)
  }

  fun getSsoRequestScopes(id: UUID): Set<Scope> {
    val ssoRequest = ssoRequestRepository.findById(id)
    if (ssoRequest.isPresent) {
      return ssoRequest.get().client.scopes
    } else {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
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
        throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
      }
    } else {
      throw ApiException(ACCESS_DENIED, ACCESS_DENIED_CODE)
    }
  }
}
