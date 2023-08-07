package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
// import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.Authentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.BasicAuthentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenService
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/v1")
class TokenController(private var tokenService: TokenService, @Qualifier("basicAuthentication") private var authentication: Authentication) {

  @GetMapping("/token", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun createToken(
    @RequestParam("code", required = false) code: UUID?,
    @RequestParam("grant_type", required = false) grant: String?,
    @RequestParam("redirect_uri", required = false) redirectUri: URI?,
    @RequestParam("refresh_token") refreshToken: String?,
    @RequestParam("nonce", required = false) nonce: String?,
    @RequestHeader(HttpHeaders.AUTHORIZATION, required = true) authorization: String,
  ): ResponseEntity<Token> {
    val authenticationInfo = authentication.authenticate(authorization)
    val clientId = authenticationInfo.clientId
    val token = tokenService
      .validateRequestAndGenerateToken(code, grant, redirectUri, clientId, refreshToken, authenticationInfo, nonce)
    return ResponseEntity.status(HttpStatus.OK).body(token)
  }
}