package com.ratiotech.underwriting.api.shared.experian

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.nio.file.Paths

/**
 * Testcontainers implementation for WireMock to simulate Experian API.
 *
 * This container runs WireMock which can be configured to stub Experian API responses
 * for integration testing. It exposes port 9090 internally and waits for the admin
 * API to be available before marking the container as ready.
 *
 * The container automatically loads static stub mappings from the `wiremock/mappings/`
 * directory, providing the same default scenarios available in Docker Compose.
 * Additional stubs can be configured programmatically via the ExperianStubs helper.
 */
class ExperianWireMockContainer :
  GenericContainer<ExperianWireMockContainer>(DockerImageName.parse("wiremock/wiremock:3.3.1")) {

  init {
    withExposedPorts(9090)
    withCommand("--verbose", "--global-response-templating", "--port", "9090")
    waitingFor(Wait.forHttp("/__admin").forStatusCode(200))
    
    // Mount the wiremock mappings directory to load static stubs
    // This provides the same default scenarios as Docker Compose
    val mappingsPath = Paths.get("wiremock", "mappings").toAbsolutePath().toString()
    withFileSystemBind(mappingsPath, "/home/wiremock/mappings", BindMode.READ_ONLY)
  }

  /**
   * Gets the base URL for the WireMock server that can be used to make HTTP requests
   * to stubbed endpoints.
   *
   * @return the base URL in the format http://host:port
   */
  fun getBaseUrl(): String = "http://$host:${getMappedPort(9090)}"

  /**
   * Gets the admin URL for the WireMock server that can be used to configure stubs
   * programmatically.
   *
   * @return the admin URL in the format http://host:port/__admin
   */
  fun getAdminUrl(): String = "${getBaseUrl()}/__admin"
}
