package dev.axgr

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestClient

@SpringBootApplication
class App {

  @Bean
  fun run(client: RestClient) = CommandLineRunner {
    client.get()
      .uri("https://rickandmortyapi.com/api/character/1")
      .retrieve()
      .toBodilessEntity()
  }

  @Bean
  fun client(builder: RestClient.Builder): RestClient {
    return builder
      .requestInterceptor(LoggingInterceptor())
      .build()
  }
}

class LoggingInterceptor : ClientHttpRequestInterceptor {

  companion object {
    private val log = LoggerFactory.getLogger(LoggingInterceptor::class.java)
  }

  override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
    log(request, body)
    val response = execution.execute(request, body)
    log(response)
    return response
  }

  private fun log(request: HttpRequest, body: ByteArray) {
    val builder = StringBuilder()
    builder.appendLine("${request.method} ${request.uri}")
    request.headers.forEach { header, values ->
      builder.appendLine("$header: $values")
    }
    if (body.isNotEmpty()) {
      builder.appendLine()
      builder.appendLine(String(body))
    }

    log.info("Request:\n{}", builder.toString())
  }

  private fun log(response: ClientHttpResponse) {
    val builder = StringBuilder()
    builder.appendLine(response.statusCode.toString())
    response.headers.forEach { header, values ->
      builder.appendLine("$header: $values")
    }
    builder.appendLine()
    builder.appendLine(response.body.readAllBytes().toString(Charsets.UTF_8))

    log.info("Response:\n{}", builder.toString())
  }
}


fun main(args: Array<String>) {
  runApplication<App>(*args)
}
