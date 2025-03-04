package com.ecommerce.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {"com.ecommerce"})
public class MongoConfig extends AbstractMongoClientConfiguration {

  @Value("${spring.data.mongodb.host}")
  private String host;

  @Value("${spring.data.mongodb.port}")
  private String port;

  @Value("${spring.data.mongodb.database}")
  private String database;


  @Override
  protected String getDatabaseName() {
    return database;
  }

  @Override
  public MongoClient mongoClient() {
    ConnectionString connectionString =
        new ConnectionString(
            String.format(
                "mongodb://%s:%s/%s",
                host,    // hostname
                port,    // port
                database // database name
            )
        );

    MongoClientSettings mongoClientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .applyToConnectionPoolSettings(
                builder ->
                    builder.maxConnectionIdleTime(30, TimeUnit.SECONDS).maxSize(50).minSize(10))
            .applyToSocketSettings(
                builder ->
                    builder
                        .connectTimeout(2000, TimeUnit.MILLISECONDS)
                        .readTimeout(10000, TimeUnit.MILLISECONDS))
            .build();

    return MongoClients.create(mongoClientSettings);
  }

  @Override
  protected boolean autoIndexCreation() {
    return true;
  }
}
