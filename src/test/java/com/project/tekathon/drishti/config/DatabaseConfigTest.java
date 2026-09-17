package com.project.tekathon.drishti.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConfigTest {

    @Test
    void testNormalizesPostgresqlUri() {
        DatabaseConfig config = new DatabaseConfig();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("postgresql://dristhi_user:pk7dc739LxYaztSSFzgqcpz3D4PRVeKO@dpg-dalvd3mk1f9s7382kme0-a.oregon-postgres.render.com/dristhi");
        
        config.postProcessBeforeInitialization(dataSource, "dataSource");

        assertEquals("jdbc:postgresql://dpg-dalvd3mk1f9s7382kme0-a.oregon-postgres.render.com:5432/dristhi", dataSource.getJdbcUrl());
        assertEquals("dristhi_user", dataSource.getUsername());
        assertEquals("pk7dc739LxYaztSSFzgqcpz3D4PRVeKO", dataSource.getPassword());
        dataSource.close();
    }

    @Test
    void testPreservesStandardJdbcUrl() {
        DatabaseConfig config = new DatabaseConfig();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/dristhi");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");

        config.postProcessBeforeInitialization(dataSource, "dataSource");

        assertEquals("jdbc:postgresql://localhost:5432/dristhi", dataSource.getJdbcUrl());
        assertEquals("postgres", dataSource.getUsername());
        assertEquals("postgres", dataSource.getPassword());
        dataSource.close();
    }
}
