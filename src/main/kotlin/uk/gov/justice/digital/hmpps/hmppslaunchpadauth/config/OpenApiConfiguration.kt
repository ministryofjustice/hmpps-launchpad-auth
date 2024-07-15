package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.List

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Value("\${launchpad.auth.api-docs.development}")
  private lateinit var devUrl: String

  @Value("\${launchpad.auth.api-docs.preprod}")
  private lateinit var preProdUrl: String

  @Value("\${launchpad.auth.api-docs.prod}")
  private lateinit var prodUrl: String

  @Bean
  fun defineOpenApi(): OpenAPI? {
    val development = Server()
    development.setUrl("https://launchpad-auth-dev.hmpps.service.justice.gov.uk")
    development.setDescription("Development")

    val preprod = Server()
    preprod.setUrl("https://launchpad-auth-preprod.hmpps.service.justice.gov.uk")
    preprod.setDescription("Pre-prod")

    val prod = Server()
    prod.setUrl("https://launchpad-auth.hmpps.service.justice.gov.uk")
    prod.setDescription("Prod")

    val contact = Contact()
    contact.setName("Launchpad Team")
    contact.setEmail("xyz@gmail.com")
    val information: Info = Info()
      .title("Launchpad Auth API")
      .version(version)
      .description("This API exposes endpoints to integrate with Launchpad Auth")
      .contact(contact)
    return OpenAPI().info(information).servers(List.of(development, preprod, prod))
  }
}
