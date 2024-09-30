package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.*

data class SarContentDto(
  val id: UUID,
  val name: String,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val firstLogInDate: LocalDateTime,
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  val lastLogInDate: LocalDateTime,
  val permissionsGranted: List<HumanReadable>,
)

data class HumanReadable(
  val humanReadable: String,
)
