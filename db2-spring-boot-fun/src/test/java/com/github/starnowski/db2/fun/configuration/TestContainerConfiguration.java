package com.github.starnowski.db2.fun.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import java.time.Duration;

@Configuration
public class TestContainerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TestContainerConfiguration.class.getName());

    @Bean
    public Db2Container db2Container(){
        Db2Container container = new Db2Container("icr.io/db2_community/db2")
                .withPrivilegedMode(true)
                .withInitScript("import.sql")
                .withReuse(true)
                .withLogConsumer(new Slf4jLogConsumer(logger))
                .withStartupTimeout(Duration.ofMinutes(16))
                .acceptLicense();
        container.start();
        return container;
    }
}
