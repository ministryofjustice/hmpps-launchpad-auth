package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.dto

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.servlet.view.RedirectView

@Hidden
class View(private val url: String) : RedirectView(url)
