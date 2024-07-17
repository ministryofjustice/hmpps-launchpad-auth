package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.ApiError
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.Token
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.authentication.Authentication
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token.TokenService
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/v1/oauth2")
@Tag(name = "token")
class TokenController(
  private var tokenService: TokenService,
  @Qualifier("basicAuthentication") private var authentication: Authentication,
) {

  companion object {
    private val logger = LoggerFactory.getLogger(TokenController::class.java)
  }

  @Operation(summary = "Get a token.", description = "Exchange an `authorization_code` or `refresh_token` with a token.")
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
      ),
      ApiResponse(
        responseCode = "400",
        content = [Content(schema = Schema(implementation = ApiError::class))],
      ),
      ApiResponse(
        responseCode = "401",
        content = [Content(schema = Schema(implementation = ApiError::class))],
      ),
      ApiResponse(
        responseCode = "403",
        content = [Content(schema = Schema(implementation = ApiError::class))],
      ),
    ],
  )
  @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun generateToken(
    @Parameter(required = false, description = "Clients need to send the `authorization_code` if requesting a token through the `authorization_code` grant.")
    @RequestParam(required = false) code: UUID?,
    @Parameter(required = true, description = "Either `authorization_code` or `refresh_token` grant.", example = "authorization_code")
    @RequestParam("grant_type") grant: String,
    @Parameter(required = false, description = "This is the same URI used to obtain the `authorization_code`. Clients need to send the `redirect_uri` if requesting a token through the `authorization_code` grant.")
    @RequestParam("redirect_uri", required = false) redirectUri: URI?,
    @Parameter(required = false, description = "Clients need to send the `refresh_token` if requesting a token through the `refresh_token` grant.")
    @RequestParam("refresh_token", required = false) refreshToken: String?,
    @Parameter(required = false, description = "Clients can send a `nonce` if requesting a token through the `refresh_token` grant.")
    @RequestParam(required = false) nonce: String?,
    @Parameter(required = true, description = "HTTP Basic authentication header with the client Id as the username and client secret as the password.", example = "Basic MGRkM...")
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
