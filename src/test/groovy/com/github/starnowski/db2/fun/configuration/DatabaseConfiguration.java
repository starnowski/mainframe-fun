package com.github.starnowski.db2.fun.configuration;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.Db2Container;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {

    @Bean
    public DataSource dataSource(Db2Container db2Container)
    {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.ibm.db2.jcc.DB2Driver");
        dataSourceBuilder.url(db2Container.getJdbcUrl());
        dataSourceBuilder.username(db2Container.getUsername());
        dataSourceBuilder.password(db2Container.getPassword());
        return dataSourceBuilder.build();
    }
}
