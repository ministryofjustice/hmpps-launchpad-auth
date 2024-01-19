package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.ehcache.event.CacheEvent
import org.ehcache.event.CacheEventListener
import org.slf4j.LoggerFactory

class EhCacheLogger : CacheEventListener<Object, Object> {

  private val logger = LoggerFactory.getLogger(EhCacheLogger::class.java)
  override fun onEvent(event: CacheEvent<out Object, out Object>?) {
    TODO("Not yet implemented")
    if (event != null) {
      logger.info("Key: {} | EventType: {} | Old value: {} | New value: {}",
        event.getKey(), event.getType(), event.getOldValue(),
        event.getNewValue())
    };

  }
}