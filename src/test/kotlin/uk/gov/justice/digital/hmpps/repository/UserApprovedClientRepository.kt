package uk.gov.justice.digital.hmpps.repository

import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository.UserApprovedClientRepository
import uk.gov.justice.digital.hmpps.utils.DataGenerator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@SpringBootTest(classes = [UserApprovedClientRepository::class])
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository"])
@EntityScan("uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model")
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class UserApprovedClientRepository(@Autowired private var userApprovedClientRepository: UserApprovedClientRepository) {

  @Test
  fun `create user aprroved client`() {
    val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    val result = userApprovedClientRepository.save(expected)
    assertUserApprovedClient(expected, result)
  }

  @Test
  fun `update user aprroved client`() {
    val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    userApprovedClientRepository.save(expected)
    val record = userApprovedClientRepository.findById(expected.id).get()
    assertUserApprovedClient(expected, record)
    record.scopes = setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_DELETE)
    record.lastModifiedDate = LocalDateTime.now(ZoneOffset.UTC)
    val result = userApprovedClientRepository.save(record)
    assertEquals(setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_DELETE), result.scopes)
  }

  @Test
  fun `get user aprroved client by id`() {
    val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    userApprovedClientRepository.save(expected)
    val result = userApprovedClientRepository.findById(expected.id)
    assertUserApprovedClient(expected, result.get())
  }

  @Test
  fun `test unique index for userid and client id`() {
    val clientId = UUID.randomUUID()
    val expectedFirst = DataGenerator.buildUserApprovedClient("test@moj.com", clientId, setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    userApprovedClientRepository.save(expectedFirst)
    val expectedSecond = DataGenerator.buildUserApprovedClient("test@moj.com", clientId, setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    assertThrows(DataIntegrityViolationException::class.java) {
      userApprovedClientRepository.save(expectedSecond)
    }
  }

  @Test
  fun `delete user aprroved client by id`() {
    val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    userApprovedClientRepository.save(expected)
    userApprovedClientRepository.deleteById(expected.id)
    val result = userApprovedClientRepository.findById(expected.id)
    assertTrue(result.isEmpty)
  }

  @Test
  fun `get user aprroved client by user id and client id`() {
    val expected = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    userApprovedClientRepository.save(expected)
    val result = userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(expected.userId, expected.clientId)
    assertUserApprovedClient(expected, result.get())
  }

  @Test
  fun `get user approved clients by user id`() {
    val expectedFirst = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    val expectedSecond = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    val expectedThird = DataGenerator.buildUserApprovedClient("test@moj.com", UUID.randomUUID(), setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ))
    userApprovedClientRepository.saveAll(listOf(expectedFirst, expectedSecond, expectedThird))
    var result = userApprovedClientRepository.findAllByUserId("test@moj.com", PageRequest.of(0, 2))
    assertEquals(2, result.size)
    result = userApprovedClientRepository.findAllByUserId("test@moj.com", PageRequest.of(1, 2))
    assertEquals(1, result.size)
    result = userApprovedClientRepository.findAllByUserId("test@moj.com", PageRequest.of(2, 2))
    assertEquals(0, result.size)
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