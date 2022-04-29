package com.example.webpos.config;

import com.example.webpos.db.MongoPosDB;
import com.example.webpos.db.PosDB;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PosConfig {

    @Bean
    public PosDB posDB(MongoPosDB mongo) {
        return mongo;
    }

}
