package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.analytics

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.integration.prisonerapi.model.Establishment
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class TelemetryService(private var telemetryClient: TelemetryClient) {

  companion object {
    private val FORMATTER = DateTimeFormatter.ISO_DATE_TIME
    private val logger = LoggerFactory.getLogger(TelemetryService::class.java)
  }

  fun addTelemetryData(eventType: AppInsightEventType, signInUser: LinkedHashMap<String, Any>) {
    try {
      val map = LinkedHashMap<String, String>()
      val dateTime = LocalDateTime.now(ZoneOffset.UTC)
      val clientId = signInUser["aud"] as String
      val userId = signInUser["sub"] as String
      map["userId"] = userId
      map["dateTime"] = dateTime.format(FORMATTER)
      map["clientId"] = clientId
      if (signInUser["establishment"] != null) {
        val establishment: Any? = signInUser.get("establishment")
        if (establishment is Establishment && establishment != null) {
          val agencyId = establishment.agencyId
          map["establishment"] = agencyId
        }
      }
      telemetryClient.trackEvent(eventType.toString(), map, null)
    } catch (e: Exception) {
      logger.error("Issue sending telemetry data: ${e.message}")
    }
  }
}
