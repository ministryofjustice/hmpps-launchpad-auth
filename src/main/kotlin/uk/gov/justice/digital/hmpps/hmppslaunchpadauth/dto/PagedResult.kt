package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

data class PagedResult<T>(
  val page: Int,
  val exhausted: Boolean,
  val totalElements: Long,
  val content: List<T>,
)
