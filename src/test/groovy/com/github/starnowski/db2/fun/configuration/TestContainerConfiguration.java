package com.github.starnowski.db2.fun.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.Db2Container;

@Configuration
public class TestContainerConfiguration {

    @Bean
    public Db2Container db2Container(){
        Db2Container container = new Db2Container("icr.io/db2_community/db2")
                .withPrivilegedMode(true)
                .withInitScript("import.sql")
                .withReuse(true)
                .acceptLicense();
        container.start();
        return container;
    }
}
