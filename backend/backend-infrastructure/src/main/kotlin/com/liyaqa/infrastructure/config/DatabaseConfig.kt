package com.liyaqa.infrastructure.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

/**
 * Database configuration for the Liyaqa Gym Management System.
 *
 * This configuration sets up:
 * - HikariCP connection pooling for optimal performance
 * - JPA/Hibernate entity management
 * - Transaction management with proper isolation levels
 * - Flyway database migrations
 * - Multi-tenant data source routing support
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["com.liyaqa.infrastructure.persistence"],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
class DatabaseConfig {

    /**
     * Primary DataSource configuration using HikariCP.
     * HikariCP is a high-performance JDBC connection pool.
     *
     * Configuration is loaded from application.yml under spring.datasource.hikari
     */
    @Primary
    @Bean(name = ["dataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    fun dataSource(): DataSource {
        val hikariConfig = HikariConfig()

        // Connection pool settings - configured via application.yml
        // These values will be overridden by the @ConfigurationProperties binding
        hikariConfig.maximumPoolSize = 10
        hikariConfig.minimumIdle = 5
        hikariConfig.connectionTimeout = 30000
        hikariConfig.idleTimeout = 600000
        hikariConfig.maxLifetime = 1800000
        hikariConfig.isAutoCommit = false

        // Performance optimizations
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true")
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true")
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true")
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true")
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true")
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true")
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false")

        return HikariDataSource(hikariConfig)
    }

    /**
     * EntityManagerFactory configuration for JPA.
     * Manages the lifecycle of JPA entities and provides persistence context.
     */
    @Primary
    @Bean(name = ["entityManagerFactory"])
    fun entityManagerFactory(
        @Qualifier("dataSource") dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val entityManagerFactory = LocalContainerEntityManagerFactoryBean()

        entityManagerFactory.dataSource = dataSource
        entityManagerFactory.setPackagesToScan(
            "com.liyaqa.domain.model",
            "com.liyaqa.infrastructure.persistence.entity"
        )

        val vendorAdapter = HibernateJpaVendorAdapter()
        vendorAdapter.setGenerateDdl(false)
        vendorAdapter.setShowSql(false) // Controlled by application.yml
        entityManagerFactory.jpaVendorAdapter = vendorAdapter

        // JPA properties
        val properties = HashMap<String, Any>()
        properties["hibernate.dialect"] = "org.hibernate.dialect.PostgreSQLDialect"
        properties["hibernate.hbm2ddl.auto"] = "validate"
        properties["hibernate.show_sql"] = "false" // Controlled by application.yml
        properties["hibernate.format_sql"] = "true"
        properties["hibernate.use_sql_comments"] = "true"
        properties["hibernate.jdbc.batch_size"] = "20"
        properties["hibernate.order_inserts"] = "true"
        properties["hibernate.order_updates"] = "true"
        properties["hibernate.jdbc.time_zone"] = "UTC"
        properties["hibernate.connection.provider_disables_autocommit"] = "true"

        // Enable second-level cache (optional - can be configured later)
        properties["hibernate.cache.use_second_level_cache"] = "false"
        properties["hibernate.cache.use_query_cache"] = "false"

        // Optimistic locking
        properties["hibernate.jdbc.batch_versioned_data"] = "true"

        entityManagerFactory.setJpaPropertyMap(properties)

        return entityManagerFactory
    }

    /**
     * Transaction Manager configuration.
     * Manages database transactions with proper isolation levels and rollback behavior.
     */
    @Primary
    @Bean(name = ["transactionManager"])
    fun transactionManager(
        @Qualifier("entityManagerFactory") entityManagerFactory: LocalContainerEntityManagerFactoryBean
    ): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory.`object`
        return transactionManager
    }

    /**
     * Flyway configuration for database migrations.
     * Ensures database schema is up-to-date with the application version.
     */
    @Bean
    fun flywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            // Repair Flyway metadata table if needed (useful for development)
            // flyway.repair()

            // Run migrations
            flyway.migrate()
        }
    }

    /**
     * Custom Flyway bean configuration.
     * Provides fine-grained control over migration execution.
     */
    @Bean(initMethod = "migrate")
    fun flyway(@Qualifier("dataSource") dataSource: DataSource): Flyway {
        return Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .validateOnMigrate(true)
            .outOfOrder(false)
            .cleanDisabled(true) // Prevent accidental data loss
            .load()
    }
}

/**
 * Multi-tenant data source routing configuration.
 * Allows the application to route database queries to different data sources
 * based on the current tenant context.
 *
 * This is useful for multi-tenant SaaS applications where each organization
 * may have its own database or schema.
 */
@Configuration
class MultiTenantConfig {

    /**
     * Thread-local storage for current tenant identifier.
     * Each request thread can have its own tenant context.
     */
    object TenantContext {
        private val currentTenant = ThreadLocal<String>()

        fun setTenantId(tenantId: String) {
            currentTenant.set(tenantId)
        }

        fun getTenantId(): String {
            return currentTenant.get() ?: "default"
        }

        fun clear() {
            currentTenant.remove()
        }
    }

    /**
     * Custom DataSource router that determines which DataSource to use
     * based on the current tenant context.
     */
    class TenantAwareRoutingDataSource : org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource() {
        override fun determineCurrentLookupKey(): Any {
            return TenantContext.getTenantId()
        }
    }
}
