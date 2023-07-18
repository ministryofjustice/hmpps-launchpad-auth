package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserApprovedClientServiceTest {
    @Mock
    private lateinit var userApprovedClientRepository: UserApprovedClientRepository

    @Mock
    private lateinit var clientService: ClientService

    private lateinit var userApprovedClientService: UserApprovedClientService


    @BeforeEach
    fun setUp() {
      userApprovedClientService = UserApprovedClientService(userApprovedClientRepository, clientService)
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun createUserApprovedClient() {
      val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_BOOKING_READ))
      Mockito.`when`(userApprovedClientRepository.save(expected)).thenReturn(expected)
      val result = userApprovedClientService.createUserApprovedClient(expected)
      assertUserApprovedClient(expected, result)
    }

    @Test
    fun updateUserApprovedClient() {
      val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
      expected.scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_DELETE)
      expected.lastModifiedDate = LocalDateTime.now(ZoneOffset.UTC)
      Mockito.`when`(userApprovedClientRepository.save(expected)).thenReturn(expected)
      val result = userApprovedClientService.updateUserApprovedClient(expected)
      assertEquals(expected, result)
    }

    @Test
    fun getUserApprovedClientById() {
      val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
      Mockito.`when`(userApprovedClientRepository.findById(expected.id)).thenReturn(Optional.of(expected))
      val result = userApprovedClientService.getUserApprovedClientById(expected.id)
      assertEquals(expected, result.get())
    }

    @Test
    fun deleteUserApprovedClientById() {
      val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
      Mockito.doNothing().`when`(userApprovedClientRepository).deleteById(expected.id)
      val result = userApprovedClientService.deleteUserApprovedClientById(expected.id)
    }

    @Test
    fun getUserApprovedClientsByUserId() {
      val pageRequest = PageRequest.of(0,1)
      val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
      Mockito.`when`(userApprovedClientRepository.findAllByUserId("test@moj.com", pageRequest)).thenReturn(listOf(expected))
      val result = userApprovedClientService.getUserApprovedClientsByUserId(expected.userId, 0, 1)
      assertEquals(1, result.totalElements)
      assertTrue(result.exhausted)
      assertEquals(0, result.page)
    }

    @Test
    fun getUserApprovedClientByUserIdAndClientId() {
    }

    @Test
    fun revokeClientAccess() {
    }

  private fun assertUserApprovedClient(expected: UserApprovedClient, result: UserApprovedClient) {
    assertEquals(expected.id, result.id)
    assertEquals(expected.userId, result.userId)
    assertEquals(expected.clientId, result.clientId)
    assertEquals(expected.createdDate, result.createdDate)
    assertEquals(expected.lastModifiedDate, result.lastModifiedDate)
    assertEquals(expected.scopes, result.scopes)
  }
}