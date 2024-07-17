package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import io.swagger.v3.oas.annotations.media.Schema

data class PagedResult<T>(
  @Schema(example = "1")
  val page: Int,
  val exhausted: Boolean,
  @Schema(example = "1")
  val totalElements: Long,
  val content: List<T>,
)
