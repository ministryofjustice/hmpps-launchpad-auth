package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Value("\${launchpad.auth.base-url.dev}")
  private lateinit var devUrl: String

  @Value("\${launchpad.auth.base-url.preprod}")
  private lateinit var preProdUrl: String

  @Value("\${launchpad.auth.base-url.prod}")
  private lateinit var prodUrl: String

  @Bean
  fun defineOpenApi(): OpenAPI? {
    val dev = Server()
    dev.url(devUrl)
    dev.description("Dev")

    val preprod = Server()
    preprod.url(preProdUrl)
    preprod.description("Pre-prod")

    val prod = Server()
    prod.url(prodUrl)
    prod.description("Prod")

    val contact = Contact()
    contact.name("Launchpad Team")
    val information: Info = Info()
      .title("Launchpad Auth")
      .version(version)
      .description("Microservice that provides Single Sign-On (SSO) capabilities to prisoner-facing clients that integrate with the Launchpad.")
      .contact(contact)
    return OpenAPI().info(information).servers(listOf(dev, preprod, prod))
  }
}
