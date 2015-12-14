package com.epam.indigoeln.config;

import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;

@Configuration
public class MongoConfig {
    @Value("${spring.data.mongodb.uri}")
    private String mongodbUri;

    public @Bean
    MongoDbFactory mongoDbFactory() throws Exception {
        MongoClientURI uri = new MongoClientURI(mongodbUri);
        return new SimpleMongoDbFactory(uri);
    }

    public @Bean
    MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        return mongoTemplate;
    }
}