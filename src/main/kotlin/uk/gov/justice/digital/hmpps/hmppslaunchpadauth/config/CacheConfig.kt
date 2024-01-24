package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.event.EventType
import org.ehcache.jsr107.Eh107Configuration
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import javax.cache.Caching

@Configuration
class CacheConfig {

  @Bean
  fun cacheManager(): JCacheCacheManager {
    val cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder.newEventListenerConfiguration(
      HmppsAuthTokenCacheEventListener(),
      EventType.CREATED,
      EventType.EXPIRED,
    ).unordered().asynchronous()
    val cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(
      String::class.java,
      String::class.java,
      ResourcePoolsBuilder.heap(1),
    ).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(3540)))
      .withService(cacheEventListenerConfiguration)
      .build()
    val cachingProvider = Caching.getCachingProvider()
    val cacheManager = cachingProvider.cacheManager
    val configuration = Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfiguration)
    cacheManager.createCache("hmpps-auth-token", configuration)
    return JCacheCacheManager(cacheManager)
  }
}
