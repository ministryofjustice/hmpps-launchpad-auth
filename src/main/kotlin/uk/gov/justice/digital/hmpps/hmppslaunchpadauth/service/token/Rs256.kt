package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.service.token

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.jackson.io.JacksonSerializer
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiErrorTypes
import uk.gov.justice.digital.hmpps.hmppslaunchpadauth.exception.ApiException
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

class Rs256
private val mapper = Jackson2ObjectMapperBuilder()
  fun main(args: Array<String>) {
    val payloadMap = HashMap<String, Any>()
    payloadMap["firstName"] = "Varun"
    payloadMap["lastName"] = "Kumar"

    val headerMap = HashMap<String, Any>()
    headerMap["id"] = UUID.randomUUID()
    var secret =
      "-----BEGIN PRIVATE KEY-----\n" +
        "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\n" +
        "D7hCji98xbM+Jx2r1Yk3MQ5ohGXeNHn3\n" +
        "-----END PRIVATE KEY-----"
    //secret = secret.replace("\n", "")
    secret = "-----BEGIN PRIVATE KEY----- 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 D7hCji98xbM+Jx2r1Yk3MQ5ohGXeNHn3 -----END PRIVATE KEY----- "
    val token = generateJwtToken(payloadMap, headerMap, secret)
    println("token=$token")

    var publicKey =
      "-----BEGIN PUBLIC KEY-----\n" +
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAub/aHa/xd4APscmOJrLN\n" +
        "gDr4l0Hw03Rfx08NxmRol3d4E5esAQf9X8u877CE7pfvlc9b0Oidzgra083TV8XG\n" +
        "Sc8QCFUmHVe6jI5vqczk9AJqy42n6J0mUmB3TfTBMHZMtSRINakW8wwVB+urO5So\n" +
        "RXVdA5qOQ5UJQLBUHE2fzAeTu/RfwLxmxbAxscXt3kolwylAuGgeo8FAEmtHJHXv\n" +
        "YdHBPcFqvLfkjaNlPX50XZmSXJB4GHetAeSC5gAFcHgU2SO1kJpwOxXYQLRRZ3zm\n" +
        "4b4nJlbNjXUP28A4n+bLz1SsqDmN9BI14dsl6tkr8yd6hYT7LQibUXwLMPfRtCDx\n" +
        "r77ie1zNf8nr3UVmagxK5hSP515ZS9fcn4IRuvOnHJUYBTOGKUN82cPtg8ajFGrd\n" +
        "UxidIsLgLbAOceE/JLroOL9wtlRiMdkKLjUEtPUrd5BmAsCbFVvM3NftHJbKCupE\n" +
        "2CBuAeuPUjfxy3SAVmpj+2O7oJIXNDHqCLsOFN90PMBGrfzedtBGqKZ3ceoANNEl\n" +
        "7XZeKNPTjQ7r+k3AR14R87HAA8tNMCuwpBX0Ut3PRYe4l0j9/ZbE35zcwc3ZoED3\n" +
        "kSmxXFG+Vm7bqtqc2q4BYsdL6q73Ge2o+GQIJnSSSoPsHluqMuba4DeEiEEAol0s\n" +
        "FdUhvZY0vnY4LY5LW5lu6scCAwEAAQ==\n" +
        "-----END PUBLIC KEY-----"
    publicKey =
      "-----BEGIN PUBLIC KEY----- MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAub/aHa/xd4APscmOJrLN gDr4l0Hw03Rfx08NxmRol3d4E5esAQf9X8u877CE7pfvlc9b0Oidzgra083TV8XG Sc8QCFUmHVe6jI5vqczk9AJqy42n6J0mUmB3TfTBMHZMtSRINakW8wwVB+urO5So RXVdA5qOQ5UJQLBUHE2fzAeTu/RfwLxmxbAxscXt3kolwylAuGgeo8FAEmtHJHXv YdHBPcFqvLfkjaNlPX50XZmSXJB4GHetAeSC5gAFcHgU2SO1kJpwOxXYQLRRZ3zm 4b4nJlbNjXUP28A4n+bLz1SsqDmN9BI14dsl6tkr8yd6hYT7LQibUXwLMPfRtCDx r77ie1zNf8nr3UVmagxK5hSP515ZS9fcn4IRuvOnHJUYBTOGKUN82cPtg8ajFGrd UxidIsLgLbAOceE/JLroOL9wtlRiMdkKLjUEtPUrd5BmAsCbFVvM3NftHJbKCupE 2CBuAeuPUjfxy3SAVmpj+2O7oJIXNDHqCLsOFN90PMBGrfzedtBGqKZ3ceoANNEl 7XZeKNPTjQ7r+k3AR14R87HAA8tNMCuwpBX0Ut3PRYe4l0j9/ZbE35zcwc3ZoED3 kSmxXFG+Vm7bqtqc2q4BYsdL6q73Ge2o+GQIJnSSSoPsHluqMuba4DeEiEEAol0s FdUhvZY0vnY4LY5LW5lu6scCAwEAAQ== -----END PUBLIC KEY-----"
    var p =
     // "-----BEGIN PUBLIC KEY-----" +
      "MIIC/jCCAeagAwIBAgIJAKysonliFZLIMA0GCSqGSIb3DQEBCwUAMC0xKzApBgNVBAMTImFjY291bnRzLmFjY2Vzc2NvbnRyb2wud2luZG93cy5uZXQwHhcNMjQwMjA4MTcwMjUzWhcNMjkwMjA4MTcwMjUzWjAtMSswKQYDVQQDEyJhY2NvdW50cy5hY2Nlc3Njb250cm9sLndpbmRvd3MubmV0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvRIL3aZt+xVqOZgMOr71ltWe9YY2Wf/B28C4Jl2nBSTEcFnf/eqOHZ8yzUBbLc4Nti2/ETcCsTUNuzS368BWkSgxc45JBH1wFSoWNFUSXaPt8mRwJYTF0H32iNhw/tBb9mvdQVgVs4Ci0dVJRYiz+ilk3PeO8wzlwRuwWIsaKFYlMyOKG9DVFbg93DmP5Tjq3C3oJlATyhAiJJc1T2trEP8960an33dDEaWwVAHh3c/34meAO4R6kLzIq0JnSsZMYB9O/6bMyIlzxmdZ8F442SynCUHxhnIh3yZew+xDdeHr6Ofl7KeVUcvSiZP9X44CaVJvknXQbBYNl+H7YF5RgQIDAQABoyEwHzAdBgNVHQ4EFgQU8Sqmrf0UFpZbGtl5y1CjUdQq5ycwDQYJKoZIhvcNAQELBQADggEBAA57FiIOUs5yyLD6a6rWCbQ4Z2XJTfQb+TM/tZ6V6QqNhSS+Q98KFOIWe9Sit0iAyDsCCKuA8f08PYnUiHmHq8dG/7YRTShE/3zCZXHYKJgMaBhYfS788zQuq/hXDdVVc5X0pZwM4ibc6+2XpcpeDHxpMOLwo2AwujDdHVLzedAkIaTCzwPIizP4LB6l6IxR+xRXsH/1f034Gk3ReAEGgHW12NkajtXmC3DKl6vGIHvx1PgAMWQbxq3F2OopNx6aZEIIZWcMpQZ6/62f3pxRJHzZiJZN+khV8hpNjJvCNf6/hNbxkLcjLAycjW8AttcCRSTM4F+02S3TyHmoE4pYywA="
     //"-----END PUBLIC KEY-----"
    p = String(Base64.getDecoder().decode(p.toByteArray(Charsets.UTF_8)))
      //p = p.replace("\n", "")
    val valid = validateJwtTokenSignature(token, publicKey)

    println(valid)
    /*val rsaGenerator = KeyPairGenerator.getInstance("RSA")
    rsaGenerator.initialize(2048)
    val keypair = rsaGenerator.genKeyPair()
    val private = keypair.private
   // println(private)
    val public= keypair.public
   println(public)
    val t = generateJwtToken(payloadMap, headerMap, private.encoded)
    println("token=$t")
    println(public)
    val valid = validateJwtTokenSignature(t, public.encoded)
    println(valid)*/

  }

fun generateJwtToken(
  payloadMap: HashMap<String, Any>,
  headerMap: HashMap<String, Any>,
  secret: String,
): String {
  try {
    val privateKeyFormatted = secret
      .trimIndent()
      .replace("-----BEGIN PRIVATE KEY-----", "")
      .replace("-----END PRIVATE KEY-----", "")
      .replace("\\s".toRegex(), "")
    val privateKeyInBytes = Base64.getDecoder().decode(privateKeyFormatted)
    val x =  KeyFactory.getInstance("RSA").generatePrivate(
      PKCS8EncodedKeySpec(privateKeyInBytes),
    )
    return Jwts.builder()
      .serializeToJsonWith(JacksonSerializer(mapper.build()))
      .addClaims(payloadMap)
      .setHeader(headerMap)
      .signWith(x, SignatureAlgorithm.RS256)
      .compact()
  } catch (e: Exception) {
    val message = "Exception during token creation ${e.message}"
    throw ApiException(message, HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorTypes.SERVER_ERROR.toString(), "Exception during token creation")
  }
}

fun validateJwtTokenSignature(token: String, secret: String): Boolean {
  val privateKeyFormatted = secret
    .trimIndent()
    .replace("-----BEGIN PUBLIC KEY-----", "")
    .replace("-----END PUBLIC KEY-----", "")
    .replace("\\s".toRegex(), "")
  val ppublicKeyInBytes = Base64.getDecoder().decode(privateKeyFormatted)
 // val cert = X509CertImpl(secret.toByteArray(Charsets.UTF_8)).
  //val pk = cert.publicKey
  val x =  KeyFactory.getInstance("RSA").generatePublic(
   // X509EncodedKeySpec(pk)
   // X509Certificate(privateKeyFormatted)
    X509EncodedKeySpec(ppublicKeyInBytes),
  )
  if (!token.contains(".")) {
    invalidTokenFormat(token)
  }
  val chunks = token.split(".")
  if (chunks.size != 3) {
    invalidTokenFormat(token)
  }
  val secretKeySpec = x//SecretKeySpec(secret.toByteArray(Charsets.UTF_8), SignatureAlgorithm.HS256.value)
  return DefaultJwtSignatureValidator(SignatureAlgorithm.RS256, secretKeySpec, Decoders.BASE64URL).isValid(
    chunks[0] + "." + chunks[1],
    chunks[2],
  )
}

private fun invalidTokenFormat(token: String) {
  val message = "Invalid bearer token format $token"
  throw ApiException(message, HttpStatus.FORBIDDEN, ApiErrorTypes.INVALID_TOKEN.toString(), "Invalid token")
}
