package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.INTERNAL_SERVER_ERROR_MSG
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.SsoException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.SsoRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.SsoRequestRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


@Service
class SsoRequestService(
  private var ssoRequestRepository: SsoRequestRepository,
) {
  companion object {
    private val logger = LoggerFactory.getLogger(SsoRequestService::class.java)
  }
  fun createSsoRequest(ssoRequest: SsoRequest): SsoRequest {
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
    var count = 0
    while (ssoRequestRecord.isPresent) {
      count += 1
      if (count > 3) {
        val message = "Duplicate uuid created multiple time for auth code"
        throw SsoException(
          message,
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          ApiErrorTypes.SERVER_ERROR.toString(),
          INTERNAL_SERVER_ERROR_MSG,
          redirectUri,
          state,
          )
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
