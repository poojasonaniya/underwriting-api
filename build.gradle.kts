import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.MetricType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application

  id("org.springframework.boot") version "3.0.8"
  id("io.spring.dependency-management") version "1.1.2"
  id("com.diffplug.spotless") version "7.2.1"
  id("io.freefair.lombok") version "6.6.1"
  id("org.flywaydb.flyway") version "9.11.0"
  id("org.jetbrains.kotlinx.kover") version "0.7.6"

  kotlin("jvm") version "1.9.0"
  kotlin("plugin.spring") version "1.9.0"
  kotlin("plugin.jpa") version "1.9.0"
}

group = "com.ratiotech.underwriting"

java {
  sourceCompatibility = JavaVersion.VERSION_17
}

application {
  mainClass.set("com.ratiotech.underwriting.api.UnderwritingApiApplication")
}

tasks.jar { enabled = false }
tasks.bootDistTar { enabled = false }
tasks.bootDistZip { enabled = false }

repositories {
  mavenCentral {
    content {
      excludeGroupByRegex("com\\.ratiotech.*")
    }
  }

  mavenLocal()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.retry:spring-retry")

  implementation("com.github.java-json-tools:json-patch:1.13")
  implementation("org.flywaydb:flyway-core:9.11.0")

  runtimeOnly("org.postgresql:postgresql")

  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("jakarta.validation:jakarta.validation-api:3.1.1")
  implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  implementation(platform("org.testcontainers:testcontainers-bom:1.19.1"))

  testImplementation("org.testcontainers:testcontainers")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("io.mockk:mockk:1.13.17")
  testImplementation("com.ninja-squad:springmockk:3.1.1")
  testImplementation("org.wiremock:wiremock-standalone:3.3.1")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "17"
  }
}

tasks.withType<Test> {
  minHeapSize = "640m"
  maxHeapSize = "1536m"
  jvmArgs = listOf("-XX:MaxMetaspaceSize=512m")
  useJUnitPlatform()
}

flyway {
  baselineVersion = "0.0"
  cleanDisabled = true
  outOfOrder = true
}

koverReport {
  verify {

    rule {
      isEnabled = true

      entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

      bound {
        minValue = 100
        metric = MetricType.BRANCH
        aggregation = AggregationType.COVERED_PERCENTAGE
      }
    }
  }
}

spotless {
  isEnforceCheck = false

  java {
    googleJavaFormat()
    trimTrailingWhitespace()
    endWithNewline()
  }

  kotlin {
    ktlint()
      .editorConfigOverride(mapOf("indent_size" to 2))
    ktfmt()
      .googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
  }

  kotlinGradle {
    target("*.gradle.kts")
    ktlint()
      .editorConfigOverride(mapOf("indent_size" to 2))
    trimTrailingWhitespace()
    endWithNewline()
  }
}
