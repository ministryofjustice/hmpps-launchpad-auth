package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.checkerframework.checker.units.qual.C
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.ClientRepository
import java.util.*
@ExtendWith(MockitoExtension::class)
class ClientServiceTest {
  @Mock
  lateinit var clientRepository: ClientRepository
  lateinit var clientService: ClientService
    @BeforeEach
    fun setUp() {
      clientService = ClientService(clientRepository)
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun validateParams() {
      println("Hello test")
    }

  private fun buildParam(
    clientId: UUID,
    responseType: String,
    scope: String,
    redirectUri: String,
    state: String,
    nonce: String,) {

  }
}