package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.ehcache.event.CacheEvent
import org.ehcache.event.CacheEventListener
import org.slf4j.LoggerFactory

class HmppsAuthTokenCacheEventListener : CacheEventListener<String, String> {

  private val logger = LoggerFactory.getLogger(HmppsAuthTokenCacheEventListener::class.java)

  override fun onEvent(event: CacheEvent<out String, out String>?) {
    if (event != null) {
      logger.debug(
        "Key: {} | EventType: {} | Old value: {} | New value: {}",
        event.getKey(),
        event.getType(),
        event.getOldValue(),
        event.getNewValue(),
      )
    }
  }
}
