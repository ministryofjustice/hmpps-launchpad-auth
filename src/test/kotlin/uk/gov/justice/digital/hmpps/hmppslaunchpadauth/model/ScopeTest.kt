package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScopeTest {
  @Test
  fun `test get enums by values`() {
    val scopes = Scope.getScopesByValues(setOf("user.basic.read", "user.establishment.read"))
    assertTrue(scopes.contains(Scope.USER_BASIC_READ))
    assertTrue(scopes.contains(Scope.USER_ESTABLISHMENT_READ))
  }

  @Test
  fun `test exception thrown when any of values are illegal argument`() {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      Scope.getScopesByValues(setOf("user.basic.read", "user.establishment.read", "user.read.random"))
    }
    assertEquals(IllegalArgumentException::class.java, exception.javaClass)
  }

  @Test
  fun `clean string of scopes to convert more than one continous white space into single white space`() {
    val scopes = Scope.cleanScopes("user. basic.read   user.establishment.read")
    assertFalse(scopes.contains("  "))
  }
  @Test
  fun `get template texts by scopes`() {
    val textSet = Scope.getTemplateTextByScopes(
      setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ, Scope.USER_ESTABLISHMENT_READ,
        Scope.USER_CLIENTS_READ, Scope.USER_CLIENTS_DELETE)
    )
    assertTrue(textSet.contains("Your name"))
    assertTrue(textSet.contains("Prison booking details (tbc)"))
    assertTrue(textSet.contains("Details of your prison"))
    assertTrue(textSet.contains("Apps you’ve allowed access to"))
    assertTrue(textSet.contains("Apps you’ve removed access to"))
  }
}


