package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import java.security.PublicKey

data class JwkSets(val kid: String, val pkey: String)
