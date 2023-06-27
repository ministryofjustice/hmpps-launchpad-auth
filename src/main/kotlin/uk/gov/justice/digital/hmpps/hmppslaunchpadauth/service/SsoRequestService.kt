package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Client
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import java.net.URL
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

  fun updateSsoRequestAuthCodeAndUserId(idToken: String, state: UUID) : String {
    val ssoRequest = ssoRequestRepository.findById(state)
    if (ssoRequest.isPresent) {
      val code = UUID.randomUUID()
      val record = ssoRequest.get()
      record.authorizationCode = code
      ssoRequestRepository.save(record)
      //record.client.nonce = idToken
      return "${record.client.reDirectUri}code=$code&state=${record.client.state}"
    } else {
      throw ApiException("Access Denied", 403)
    }
  }

  private fun getSsoRequest(state: UUID): Optional<SsoRequest> {
    return ssoRequestRepository.findById(state)
  }

  fun validateAutoApprove(state: UUID): Boolean {
    val ssoRequest = getSsoRequest(state)
    if (ssoRequest.isPresent) {
      val clientId = ssoRequest.get().client.id
      val client = clientService.getClientById(clientId)
      if (client.isPresent) {
        return client.get().autoApprove
      } else {
        throw ApiException("Client not found", 400)
      }
    } else {
      throw ApiException("SsoRequest not found", 400)
    }
  }

  fun getClient(id: UUID): Client {
    val ssoRequest = getSsoRequest(id)
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