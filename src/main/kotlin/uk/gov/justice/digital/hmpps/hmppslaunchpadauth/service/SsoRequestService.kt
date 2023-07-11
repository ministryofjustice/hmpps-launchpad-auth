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
    val ssoRequestCreated =  ssoRequestRepository.save(ssoRequest)
    logger.info(String.format("Sso request created for user of  client: %s", ssoRequestCreated.client.id))
    return ssoRequestCreated
  }

  fun updateSsoRequest(ssoRequest: SsoRequest): SsoRequest {
    val updatedSsoRequest =  ssoRequestRepository.save(ssoRequest)
    logger.info(String.format("Sso request updated  for user of  client: %s", ssoRequest.client.id))
    return updatedSsoRequest
  }

  fun getSsoRequestById(id: UUID): Optional<SsoRequest> {
    logger.debug(String.format("Sso request retrieved for id: %s", id))
    return ssoRequestRepository.findById(id)
  }

  fun deleteSsoRequestById(id: UUID) {
    logger.debug(String.format("Sso request deleted for id: %s", id))
    ssoRequestRepository.deleteById(id)
  }

  fun generateSsoRequest(
    scopes: Set<Scope>,
    state: String?,
    nonce: String?,
    redirectUri: String,
    clientId: UUID,
  ): SsoRequest {
    var authorizationCode: UUID = UUID.randomUUID()
    var count:Int = ssoRequestRepository.countAuthorizationCodeByValue(authorizationCode)
    while (count != 0) {
      logger.debug("Authorization code exist in sso request db record so creating new")
      authorizationCode = UUID.randomUUID()
      count = ssoRequestRepository.countAuthorizationCodeByValue(authorizationCode)
    }
    val ssoRequest = SsoRequest(
      UUID.randomUUID(),
      UUID.randomUUID(),
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
