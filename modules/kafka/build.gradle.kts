plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api("org.springframework.kafka:spring-kafka")

    testFixturesImplementation("org.springframework.kafka:spring-kafka-test")
    testFixturesImplementation("org.testcontainers:kafka")
}
