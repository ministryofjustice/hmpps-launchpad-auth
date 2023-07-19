package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import java.time.LocalDateTime
import java.util.*

data class UserClients(
  val page: Int,
  val exhausted: Boolean,
  val totalElements: Int,
  val content: List<Client>,
)

data class Client(
  val id: UUID,
  val clientName: String,
  val logoUri: String,
  val description: String,
  val autoApprove: Boolean,
  val createdDate: LocalDateTime,
  val scopes: Set<Scope>,
)

data class Scope(
  val scope: String,
  val readableText: String,
)
