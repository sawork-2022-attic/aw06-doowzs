package com.example.webpos.config;

import com.example.webpos.db.Mongo;
import com.example.webpos.db.PosDB;
import com.example.webpos.repository.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PosConfig {

    @Bean
    public Mongo mongo(ProductRepository productRepository) {
        return new Mongo(productRepository);
    }

    @Bean
    public PosDB posDB(ProductRepository productRepository) {
        return mongo(productRepository);
    }

}
