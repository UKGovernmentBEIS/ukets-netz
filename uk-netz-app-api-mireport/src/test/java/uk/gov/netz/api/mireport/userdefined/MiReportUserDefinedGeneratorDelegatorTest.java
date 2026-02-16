package uk.gov.netz.api.mireport.userdefined;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.core.MiReportEntityManagerResolver;

@ExtendWith(MockitoExtension.class)
class MiReportUserDefinedGeneratorDelegatorTest {

	@InjectMocks
    private MiReportUserDefinedGeneratorDelegator cut;

    @Mock
    private MiReportEntityManagerResolver miReportEntityManagerResolver;
    
    @Mock
    private MiReportUserDefinedGenerator miReportUserDefinedGenerator;
    
    @Test
    void generateReport() {
    	CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
    	String sqlQuery ="sql query";
    	
    	EntityManager em = Mockito.mock(EntityManager.class);
    	when(miReportEntityManagerResolver.resolveByCA(competentAuthority)).thenReturn(em);
    
    	MiReportUserDefinedResult result = MiReportUserDefinedResult.builder()
    			.columnNames(List.of("col1"))
    			.build();
    	
    	when(miReportUserDefinedGenerator.generateMiReport(em, sqlQuery)).thenReturn(result);
    	
    	var actualResult = cut.generateReport(competentAuthority, sqlQuery);
    	
    	assertThat(actualResult).isEqualTo(result);
    	verify(miReportEntityManagerResolver, times(1)).resolveByCA(competentAuthority);
    	verify(miReportUserDefinedGenerator, times(1)).generateMiReport(em, sqlQuery);
    }
}
