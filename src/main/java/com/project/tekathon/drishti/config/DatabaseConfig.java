package com.project.tekathon.drishti.config;

import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseConfig implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof HikariDataSource dataSource) {
            String url = dataSource.getJdbcUrl();
            if (url != null && (url.startsWith("postgresql://") || url.startsWith("postgres://"))) {
                log.info("Normalizing Render PostgreSQL connection URL: {}", url);
                try {
                    String cleanUrl = url;
                    if (url.startsWith("postgres://")) {
                        cleanUrl = "postgresql://" + url.substring("postgres://".length());
                    }
                    URI uri = URI.create(cleanUrl);
                    if (uri.getUserInfo() != null) {
                        String[] userInfo = uri.getUserInfo().split(":");
                        if ((dataSource.getUsername() == null || dataSource.getUsername().isBlank() || "postgres".equals(dataSource.getUsername())) && userInfo.length > 0) {
                            dataSource.setUsername(userInfo[0]);
                        }
                        if ((dataSource.getPassword() == null || dataSource.getPassword().isBlank() || "postgres".equals(dataSource.getPassword())) && userInfo.length > 1) {
                            dataSource.setPassword(userInfo[1]);
                        }
                    }
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    String path = uri.getPath();
                    String host = uri.getHost() == null ? "" : uri.getHost();
                    String jdbcUrl = "jdbc:postgresql://" + host + (port != -1 ? ":" + port : "") + path;
                    if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                        jdbcUrl += "?" + uri.getQuery();
                    }
                    dataSource.setJdbcUrl(jdbcUrl);
                    log.info("Normalized Hikari JDBC URL to: {}", jdbcUrl);
                } catch (Exception ex) {
                    log.warn("Failed to parse URI {}, applying fallback replacement", url, ex);
                    String fallback = url.replaceFirst("^(postgresql|postgres)://", "jdbc:postgresql://");
                    dataSource.setJdbcUrl(fallback);
                }
            }
        }
        return bean;
    }
}
