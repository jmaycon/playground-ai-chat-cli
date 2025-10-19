package edu.jmaycon.playground;

import edu.jmaycon.playground.VectorStoreConfig.Props;
import lombok.NonNull;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIdType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableConfigurationProperties(Props.class)
public class VectorStoreConfig {

    @Bean
    VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel, Props p) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .idType(p.idType())
                .vectorTableName(p.tableName())
                .initializeSchema(p.initializeSchema())
                .indexType(p.indexType())
                .distanceType(p.distanceType())
                .dimensions(p.dimensions())
                .build();
    }

    @ConfigurationProperties("spring.ai.vectorstore.pgvector")
    record Props(
            @NonNull PgIdType idType,
            @NonNull String tableName,
            @NonNull Boolean initializeSchema,
            @NonNull PgIndexType indexType,
            @NonNull PgDistanceType distanceType,
            @NonNull Integer dimensions) {}
}
