package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.*

data class PagedResult<T>(
  val page: Int,
  val exhausted: Boolean,
  val totalElements: Int,
  val content: List<T>,
)

data class Client(
  val id: UUID,
  val name: String,
  val logoUri: String,
  val description: String,
  val autoApprove: Boolean,
  @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss'Z'")
  val createdDate: LocalDateTime,
  val scopes: List<Scope>,
)

data class Scope(
  val type: String,
  val humanReadable: String,
)
