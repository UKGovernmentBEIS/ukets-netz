package uk.gov.netz.api.mireport.system;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.system.executedactions.ExecutedRequestActionsMiReportParams;
import uk.gov.netz.api.mireport.system.executedactions.ExecutedRequestActionsMiReportResult;

@ExtendWith(MockitoExtension.class)
class MiReportSystemServiceTest {

    @InjectMocks
    private MiReportSystemService cut;
    
    @Mock
    private MiReportSystemGeneratorDelegator miReportSystemGeneratorDelegator;

    @Mock
    private MiReportSystemRepository miReportSystemRepository;
    
    @Mock
    private TestGenerator testGenerator;
    
    @Spy
    private ArrayList<MiReportSystemGenerator> miReportSystemGenerators;

    @BeforeEach
    void setUp() {
    	miReportSystemGenerators.add(testGenerator);
    }

    @Test
    void findByCompetentAuthority() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        MiReportSystemSearchResult expectedMiReportSearchResult = Mockito.mock(MiReportSystemSearchResult.class);

        when(miReportSystemRepository.findByCompetentAuthority(competentAuthority))
            .thenReturn(List.of(expectedMiReportSearchResult));

        List<MiReportSystemSearchResult> actual = cut.findByCompetentAuthority(competentAuthority);

        assertThat(actual.getFirst()).isEqualTo(expectedMiReportSearchResult);
    }

    @Test
    void generateReport() {
    	CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
    	ExecutedRequestActionsMiReportParams params = ExecutedRequestActionsMiReportParams.builder()
    			.reportType("reportType")
    			.build();
    	
		ExecutedRequestActionsMiReportResult result = ExecutedRequestActionsMiReportResult.builder().columnNames(List.of("col"))
				.build();
		
		when(miReportSystemGeneratorDelegator.generateReport(competentAuthority, params, miReportSystemGenerators))
				.thenReturn(result);
		
        var actualResult = cut.generateReport(competentAuthority, params);
        
        assertThat(actualResult).isEqualTo(result);
        verify(miReportSystemGeneratorDelegator, times(1)).generateReport(competentAuthority, params, miReportSystemGenerators);
    }
}
