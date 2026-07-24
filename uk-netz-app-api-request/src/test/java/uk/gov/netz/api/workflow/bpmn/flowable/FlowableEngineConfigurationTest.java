package uk.gov.netz.api.workflow.bpmn.flowable;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FlowableEngineConfigurationTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

    @Test
    void configure_with_database_schema() {
        FlowableEngineConfiguration configuration =
            new FlowableEngineConfiguration(dataSource, transactionManager, "sch_flowable");
        SpringProcessEngineConfiguration engineConfiguration = new SpringProcessEngineConfiguration();
        engineConfiguration.setDatabaseSchema("sch_flowable");

        configuration.configure(engineConfiguration);

        assertEquals("sch_flowable.", engineConfiguration.getDatabaseTablePrefix());
        assertTrue(engineConfiguration.isTablePrefixIsSchema());
        assertNull(engineConfiguration.getDatabaseSchema());
        TransactionAwareDataSourceProxy configuredDataSource =
            (TransactionAwareDataSourceProxy) engineConfiguration.getDataSource();
        assertSame(dataSource, configuredDataSource.getTargetDataSource());
        assertSame(transactionManager, engineConfiguration.getTransactionManager());
    }
}