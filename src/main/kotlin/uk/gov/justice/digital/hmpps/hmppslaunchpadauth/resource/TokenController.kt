package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.Authentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenService
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/v1/oauth2")
class TokenController(
  private var tokenService: TokenService,
  @Qualifier("basicAuthentication") private var authentication: Authentication,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(TokenController::class.java)
  }

  @Tag(name = "token", description = "Get id token, access token and refresh token by code return in sign in request")
  @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun generateToken(
    @RequestParam(required = false) code: UUID?,
    @RequestParam("grant_type", required = false) grant: String?,
    @RequestParam("redirect_uri", required = false) redirectUri: URI?,
    @RequestParam("refresh_token", required = false) refreshToken: String?,
    @RequestParam(required = false) nonce: String?,
    @RequestHeader(HttpHeaders.AUTHORIZATION, required = true) authorization: String,
  ): ResponseEntity<Token> {
    logger.info("Request received to generate token")
    val authenticationInfo = authentication.authenticate(authorization)
    val clientId = authenticationInfo.clientId
    val token = tokenService
      .validateRequestAndGenerateToken(code, grant, redirectUri, clientId, refreshToken, authenticationInfo, nonce)
    return ResponseEntity.status(HttpStatus.OK).body(token)
  }
}
