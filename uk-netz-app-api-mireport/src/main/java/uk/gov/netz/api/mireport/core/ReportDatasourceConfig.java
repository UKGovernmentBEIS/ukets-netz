package uk.gov.netz.api.mireport.core;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ReportDatasourceConfig {
    private static final String PACKAGE = "uk.gov";
    private static final Map<String, Object> hibernateProperties = new HashMap<>();

    static {
        hibernateProperties.put("hibernate.hbm2ddl.auto", "none");
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('EA')")
    @ConfigurationProperties(prefix = "report-datasource-ea")
    public DataSourceProperties reportEaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('EA')")
    @ConfigurationProperties(prefix = "report-datasource-ea.hikari")
    public HikariDataSource reportEaDataSource() {
        return reportEaDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "reportEaEntityManager", defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('EA')")
    public LocalContainerEntityManagerFactoryBean reportEaEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(reportEaDataSource())
                .persistenceUnit("reportEa")
                .packages(PACKAGE)
                .properties(hibernateProperties)
                .build();
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('SEPA')")
    @ConfigurationProperties(prefix = "report-datasource-sepa")
    public DataSourceProperties reportSepaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('SEPA')")
    @ConfigurationProperties(prefix = "report-datasource-sepa.hikari")
    public HikariDataSource reportSepaDataSource() {
        return reportSepaDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "reportSepaEntityManager", defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('SEPA')")
    public LocalContainerEntityManagerFactoryBean reportSepaEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(reportSepaDataSource())
                .persistenceUnit("reportSepa")
                .packages(PACKAGE)
                .properties(hibernateProperties)
                .build();
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('NIEA')")
    @ConfigurationProperties(prefix = "report-datasource-niea")
    public DataSourceProperties reportNieaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('NIEA')")
    @ConfigurationProperties(prefix = "report-datasource-niea.hikari")
    public HikariDataSource reportNieaDataSource() {
        return reportNieaDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "reportNieaEntityManager", defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('NIEA')")
    public LocalContainerEntityManagerFactoryBean reportNieaEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(reportNieaDataSource())
                .persistenceUnit("reportNiea")
                .packages(PACKAGE)
                .properties(hibernateProperties)
                .build();
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('OPRED')")
    @ConfigurationProperties(prefix = "report-datasource-opred")
    public DataSourceProperties reportOpredDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('OPRED')")
    @ConfigurationProperties(prefix = "report-datasource-opred.hikari")
    public HikariDataSource reportOpredDataSource() {
        return reportOpredDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "reportOpredEntityManager", defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('OPRED')")
    public LocalContainerEntityManagerFactoryBean reportOpredEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(reportOpredDataSource())
                .persistenceUnit("reportOpred")
                .packages(PACKAGE)
                .properties(hibernateProperties)
                .build();
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('NRW')")
    @ConfigurationProperties(prefix = "report-datasource-nrw")
    public DataSourceProperties reportNrwDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('NRW')")
    @ConfigurationProperties(prefix = "report-datasource-nrw.hikari")
    public HikariDataSource reportNrwDataSource() {
        return reportNrwDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "reportNrwEntityManager", defaultCandidate = false)
    @ConditionalOnExpression("T(java.util.Arrays).asList('${report-supported-cas}'.split(',')).contains('NRW')")
    public LocalContainerEntityManagerFactoryBean reportNrwEntityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(reportNrwDataSource())
                .persistenceUnit("reportNrw")
                .packages(PACKAGE)
                .properties(hibernateProperties)
                .build();
    }
}
