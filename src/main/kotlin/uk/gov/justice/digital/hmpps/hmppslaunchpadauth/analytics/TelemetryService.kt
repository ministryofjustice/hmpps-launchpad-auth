package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.analytics

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Component

@Component
class TelemetryService(private var telemetryClient: TelemetryClient) {

  fun addTelemetryData(eventName: String, signInUser: SignedInUser) {
    val map = mapOf<String, String>(
      "userId" to signInUser.userId,
      "dateTime" to signInUser.dateTime.toString(),
      "clientId" to signInUser.clientId,
      "establishment" to signInUser.establishment,
    )
    telemetryClient.trackEvent(eventName, map, null)
  }
}
