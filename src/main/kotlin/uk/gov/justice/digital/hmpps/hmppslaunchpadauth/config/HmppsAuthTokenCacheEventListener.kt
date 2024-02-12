package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.ehcache.event.CacheEvent
import org.ehcache.event.CacheEventListener
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.constant.AuthServiceConstant.Companion.HMPPS_AUTH_ACCESS_TOKEN_CACHE

class HmppsAuthTokenCacheEventListener : CacheEventListener<String, String> {

  private val logger = LoggerFactory.getLogger(HmppsAuthTokenCacheEventListener::class.java)

  override fun onEvent(event: CacheEvent<out String, out String>?) {
    if (event != null) {
      logger.info(
        "Cache $HMPPS_AUTH_ACCESS_TOKEN_CACHE entry Key: {} | EventType: {}",
        event.getKey(),
        event.getType(),
      )
    }
  }
}
