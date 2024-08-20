package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.resource

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto.UserApprovedClientDto
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.SarService
import java.time.LocalDate

@RestController
class ServiceAccessEndpoint(private var sarService: SarService) {

  @GetMapping(path = ["/subject-access-request"])
  fun getUsersData(
    @RequestParam("prn", required = false) personReferenceNumber: String,
    @RequestParam("crn", required = false) caseReferenceNumber: String,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @RequestParam(required = false) fromDate: LocalDate?,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @RequestParam(required = false) toDate: LocalDate?,
  ): ResponseEntity<List<UserApprovedClientDto>> {
    val userid: String?
    if (personReferenceNumber == null && caseReferenceNumber == null) {
      throw ApiException(
        "personReferenceNumber and caseReferenceNumber, both cannot be null",
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        "personReferenceNumber and caseReferenceNumber, both cannot be null",
      )
    }
    if (personReferenceNumber != null) {
      userid = personReferenceNumber
    } else {
      userid = caseReferenceNumber
    }
    val userApprovedClients = sarService.getUsers(userid, fromDate, toDate)
    return ResponseEntity.status(HttpStatus.OK).body(userApprovedClients)
  }
}
