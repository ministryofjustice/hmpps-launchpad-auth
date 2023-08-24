package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@WebFilter
@Component
class ResponseHeadersFilter : Filter {
  override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
    val httpServletResponse: HttpServletResponse = response as HttpServletResponse
    httpServletResponse.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate")
    httpServletResponse.setHeader(HttpHeaders.PRAGMA, "no-cache")
    httpServletResponse.setHeader(HttpHeaders.EXPIRES, "0")
    httpServletResponse.setHeader("X-Frame-Options", "DENY")
    httpServletResponse.setHeader("X-Content-Type-Options", "nosniff")
    chain?.doFilter(request, response)
  }
}
