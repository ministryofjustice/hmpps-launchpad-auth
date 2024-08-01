package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.*

@Schema(name = "UserApprovedClient")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserApprovedClientDto(
  val id: UUID,
  val name: String,
  val logoUri: String?,
  val description: String,
  val autoApprove: Boolean,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val createdDate: LocalDateTime,
  val scopes: List<Scope>,
)
