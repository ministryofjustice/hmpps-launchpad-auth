package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class SsoRequestService(
  private var ssoRequestRepository: SsoRequestRepository
) {
  private val logger = LoggerFactory.getLogger(SsoRequestService::class.java)
  fun createSsoRequest(ssoRequest: SsoRequest): SsoRequest {
    logger.info(String.format("Sso request created for user of  client: %s", ssoRequest.client.id))
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
      LocalDateTime.now(ZoneOffset.UTC),
      UUID.randomUUID(),
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
}
