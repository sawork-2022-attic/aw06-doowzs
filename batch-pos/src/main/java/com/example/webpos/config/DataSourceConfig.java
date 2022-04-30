package com.example.webpos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.batch.datasource")
    public DataSource dataSource() {
        return new DriverManagerDataSource();
    }

}
