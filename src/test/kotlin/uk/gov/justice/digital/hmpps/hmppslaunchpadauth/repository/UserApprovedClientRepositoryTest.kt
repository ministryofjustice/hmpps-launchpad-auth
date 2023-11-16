package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.Scope
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model.UserApprovedClient
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.utils.DataGenerator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@SpringBootTest(classes = [UserApprovedClientRepository::class])
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = ["uk.gov.justice.digital.hmpps.hmppslaunchpadauth.repository"])
@EntityScan("uk.gov.justice.digital.hmpps.hmppslaunchpadauth.model")
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class UserApprovedClientRepositoryTest(@Autowired private var userApprovedClientRepository: UserApprovedClientRepository) {
  private val dateAndTimeInUTC = LocalDateTime.now(ZoneOffset.UTC)
  private val userId = "G2320VD"

  @BeforeEach
  fun `set up`() {
    userApprovedClientRepository.deleteAll()
  }

  @Test
  fun `create user aprroved client`() {
    val expected = DataGenerator.buildUserApprovedClient(
      userId,
      UUID.randomUUID(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      dateAndTimeInUTC,
      dateAndTimeInUTC,
    )
    val result = userApprovedClientRepository.save(expected)
    assertUserApprovedClient(expected, result)
  }

  @Test
  fun `update user aprroved client`() {
    val expected = DataGenerator.buildUserApprovedClient(
      userId,
      UUID.randomUUID(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      dateAndTimeInUTC,
      dateAndTimeInUTC,
    )
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
    val expected = DataGenerator.buildUserApprovedClient(
      userId,
      UUID.randomUUID(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      dateAndTimeInUTC,
      dateAndTimeInUTC,
    )
    userApprovedClientRepository.save(expected)
    val result = userApprovedClientRepository.findById(expected.id)
    assertUserApprovedClient(expected, result.get())
  }

  @Test
  fun `test unique index for  created date, userid and client id`() {
    val clientId = UUID.randomUUID()
    val expectedFirst = DataGenerator.buildUserApprovedClient(
      userId,
      clientId,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      dateAndTimeInUTC,
      dateAndTimeInUTC,
    )
    userApprovedClientRepository.save(expectedFirst)
    val expectedSecond = DataGenerator.buildUserApprovedClient(
      userId,
      clientId,
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      dateAndTimeInUTC,
      dateAndTimeInUTC,
    )

    assertThrows(DataIntegrityViolationException::class.java) {
      userApprovedClientRepository.save(expectedSecond)
    }
  }

  @Test
  fun `delete user aprroved client by id`() {
    val createdDate = LocalDateTime.now(ZoneOffset.UTC)
    val expected = DataGenerator.buildUserApprovedClient(
      userId,
      UUID.randomUUID(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      createdDate,
      createdDate,
    )
    userApprovedClientRepository.save(expected)
    userApprovedClientRepository.deleteById(expected.id)
    val result = userApprovedClientRepository.findById(expected.id)
    assertTrue(result.isEmpty)
  }

  @Test
  fun `get user aprroved client by user id and client id`() {
    val expected = DataGenerator.buildUserApprovedClient(
      userId,
      UUID.randomUUID(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      dateAndTimeInUTC,
      dateAndTimeInUTC,
    )
    userApprovedClientRepository.save(expected)
    val result =
      userApprovedClientRepository.findUserApprovedClientByUserIdAndClientId(expected.userId, expected.clientId)
    assertUserApprovedClient(expected, result.get())
  }

  @Test
  fun `test get user approved clients by user id and page result is sorted by created date`() {
    val dateAndTimeInUTCFirst = LocalDateTime.now(ZoneOffset.UTC)
    val dateAndTimeInUTCSecond = dateAndTimeInUTCFirst.plusMinutes(1L)
    val dateAndTimeInUTCThird = dateAndTimeInUTCFirst.plusMinutes(2L)
    val expectedFirst = DataGenerator.buildUserApprovedClient(
      userId,
      UUID.randomUUID(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      dateAndTimeInUTCFirst,
      dateAndTimeInUTCFirst,
    )
    val expectedSecond = DataGenerator.buildUserApprovedClient(
      userId,
      UUID.randomUUID(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      dateAndTimeInUTCSecond,
      dateAndTimeInUTCSecond,
    )
    val expectedThird = DataGenerator.buildUserApprovedClient(
      userId,
      UUID.randomUUID(),
      setOf(Scope.USER_BASIC_READ, Scope.USER_CLIENTS_READ, Scope.USER_BOOKING_READ),
      dateAndTimeInUTCThird,
      dateAndTimeInUTCThird,
    )
    userApprovedClientRepository.saveAll(listOf(expectedThird, expectedSecond, expectedFirst))
    var result = userApprovedClientRepository.findUserApprovedClientsByUserIdAndClientIdsIsNotNull(
      userId,
      PageRequest.of(0, 2).withSort(Sort.Direction.DESC, "created_date"),
    )
    assertEquals(2, result.content.size)
    // assertEquals(dateAndTimeInUTCThird, result.content.get(0).createdDate)
    // assertEquals(dateAndTimeInUTCSecond, result.content.get(1).createdDate)
    assertEquals(3, result.totalElements)
    assertEquals(0, result.number)
    assertFalse(result.isLast)
    result = userApprovedClientRepository.findUserApprovedClientsByUserIdAndClientIdsIsNotNull(
      userId,
      PageRequest.of(1, 2).withSort(Sort.Direction.DESC, "created_date"),
    )
    assertEquals(1, result.content.size)
    assertEquals(dateAndTimeInUTCFirst, result.content.get(0).createdDate)
    assertEquals(3, result.totalElements)
    assertEquals(1, result.number)
    assertTrue(result.isLast)
    result = userApprovedClientRepository.findUserApprovedClientsByUserIdAndClientIdsIsNotNull(
      userId,
      PageRequest.of(2, 2).withSort(Sort.Direction.DESC, "created_date"),
    )
    assertEquals(0, result.content.size)
    assertEquals(3, result.totalElements)
    assertEquals(2, result.number)
    assertTrue(result.isLast)
  }

  private fun assertUserApprovedClient(expected: UserApprovedClient, result: UserApprovedClient) {
    assertEquals(expected.id, result.id)
    assertEquals(expected.userId, result.userId)
    assertEquals(expected.clientId, result.clientId)
    // assertEquals(expected.createdDate, result.createdDate)
    // assertEquals(expected.lastModifiedDate, result.lastModifiedDate)
    assertEquals(expected.scopes, result.scopes)
  }
}
