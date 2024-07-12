package uk.gov.justice.digital.hmpps.hmppslaunchpadauth.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.List


@Configuration
class SwaggerConfiguration {
  @Bean
  fun defineOpenApi(): OpenAPI? {
    val server = Server()
    server.setUrl("http://localhost:8080")
    server.setDescription("Development")
    val myContact = Contact()
    myContact.setName("Launchpad Team")
    myContact.setEmail("xyz@gmail.com")
    val information: Info = Info()
      .title("Launchpad Auth API")
      .version("1.0")
      .description("This API exposes endpoints to integrate with Launchpad Auth")
      .contact(myContact)
    return OpenAPI().info(information).servers(List.of(server))
  }
}