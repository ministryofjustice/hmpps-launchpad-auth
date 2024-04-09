package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.*

class JwkStore
fun main(args: Array<String>) {
  val keyPair = generateRandomRSAKey()
  println(Base64.getEncoder().encodeToString(keyPair.private.encoded))
  println(Base64.getEncoder().encodeToString(keyPair.public.encoded))
  val jwkset = JwkSets(UUID.randomUUID().toString(), keyPair.public.toString())
  val mapper = ObjectMapper()
  val set = mapper.writeValueAsString(jwkset)
  println(set)
  val e = String(Base64.getEncoder().encode(set.toByteArray()))
  val d = String(Base64.getEncoder().encode(e.toByteArray()))
  println(e)
  println(d)
}

fun generateRandomRSAKey(): KeyPair {
  val rsaGenerator = KeyPairGenerator.getInstance("RSA")
  rsaGenerator.initialize(2048)
  return rsaGenerator.genKeyPair()
}