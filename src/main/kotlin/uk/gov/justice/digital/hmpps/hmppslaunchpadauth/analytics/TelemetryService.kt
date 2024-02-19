package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.analytics

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import java.util.*

@Component
class TelemetryService(private var telemetryClient: TelemetryClient) {

  fun addTelemetryData(eventName: String, signInUser: LinkedHashMap<String, Any>) {
    val dateTime = signInUser["exp"] as Long
    val clientId = signInUser["aud"] as String
    val userId = signInUser["sub"] as String
    var agencyId: String = ""
    if (signInUser["establishment"] != null) {
      val establishment: Any? = signInUser.get("establishment")
      if (establishment is Establishment && establishment != null) {
        agencyId = establishment.agencyId
      }
    }
    val map = mapOf<String, String>(
      "userId" to userId,
      "dateTime" to dateTime.toString(),
      "clientId" to clientId,
      "establishment" to agencyId,
    )
    telemetryClient.trackEvent(eventName, map, null)
  }
}
