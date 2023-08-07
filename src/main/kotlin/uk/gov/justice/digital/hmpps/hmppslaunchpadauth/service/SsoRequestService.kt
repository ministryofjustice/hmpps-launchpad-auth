package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

const val INTERNAL_SERVER_ERROR_CODE = 500

@Service
class SsoRequestService(
  private var ssoRequestRepository: SsoRequestRepository,
) {
  private val logger = LoggerFactory.getLogger(SsoRequestService::class.java)
  fun createSsoRequest(ssoRequest: SsoRequest): SsoRequest {
    println(LocalDateTime.now())
    val ssoRequestCreated = ssoRequestRepository.save(ssoRequest)
    logger.info("Sso request created for user of  client: {}", ssoRequestCreated.client.id)
    return ssoRequestCreated
  }

  fun updateSsoRequest(ssoRequest: SsoRequest): SsoRequest {
    val updatedSsoRequest = ssoRequestRepository.save(ssoRequest)
    logger.info("Sso request updated  for user of  client: {}", ssoRequest.client.id)
    return updatedSsoRequest
  }

  fun getSsoRequestById(id: UUID): Optional<SsoRequest> {
    logger.debug("Sso request retrieved for id: {}", id)
    return ssoRequestRepository.findById(id)
  }

  fun deleteSsoRequestById(id: UUID) {
    logger.debug("Sso request deleted for id: {}", id)
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
    var ssoRequestRecord = ssoRequestRepository.findSsoRequestByAuthorizationCode(authorizationCode)
    var count: Int = 0
    while (ssoRequestRecord.isPresent) {
      count += 1
      if (count > 3) {
        throw ApiException("Duplicate uuid created multiple time for auth code", INTERNAL_SERVER_ERROR_CODE)
      }
      logger.debug("Authorization code exist in sso request db record so creating new")
      authorizationCode = UUID.randomUUID()
      ssoRequestRecord = ssoRequestRepository.findSsoRequestByAuthorizationCode(authorizationCode)
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

  fun getSsoRequestByAuthorizationCode(code: UUID): Optional<SsoRequest> {
    return ssoRequestRepository.findSsoRequestByAuthorizationCode(code)
  }
}
