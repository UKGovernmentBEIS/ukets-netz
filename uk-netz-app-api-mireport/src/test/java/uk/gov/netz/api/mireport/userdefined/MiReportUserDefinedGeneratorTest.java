package uk.gov.netz.api.mireport.userdefined;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.userinfoapi.UserInfoApi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiReportUserDefinedGeneratorTest {

    @InjectMocks
    private MiReportUserDefinedGenerator cut;

    @Mock
    private UserInfoApi userInfoApi;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Session session;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData resultSetMetaData;

    @BeforeEach
    void setUp() throws Exception {
        when(entityManager.unwrap(Session.class)).thenReturn(session);

        doAnswer(invocation -> {
            Work work = invocation.getArgument(0);
            work.execute(connection);
            return null;
        }).when(session).doWork(any(Work.class));

        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(0);
        when(resultSet.next()).thenReturn(false);
    }

    @Test
    void generateMiReport_appliesMaxRows_whenProvided() throws Exception {
        MiReportUserDefinedResult result = cut.generateMiReport(entityManager, "select * from foo", 10);

        assertThat(result).isNotNull();
        verify(preparedStatement, times(1)).setMaxRows(10);
    }

    @Test
    void generateMiReport_doesNotApplyMaxRows_whenNull() throws Exception {
        MiReportUserDefinedResult result = cut.generateMiReport(entityManager, "select * from foo");

        assertThat(result).isNotNull();
        verify(preparedStatement, never()).setMaxRows(org.mockito.ArgumentMatchers.anyInt());
    }
}