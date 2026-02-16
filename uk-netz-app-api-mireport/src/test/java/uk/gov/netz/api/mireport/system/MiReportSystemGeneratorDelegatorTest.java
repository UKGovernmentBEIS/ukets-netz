package uk.gov.netz.api.mireport.system;

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
import uk.gov.netz.api.mireport.system.executedactions.ExecutedRequestAction;
import uk.gov.netz.api.mireport.system.executedactions.ExecutedRequestActionsMiReportParams;
import uk.gov.netz.api.mireport.system.executedactions.ExecutedRequestActionsMiReportResult;
import uk.gov.netz.api.mireport.system.executedactions.ExecutedRequestActionsReportGenerator;

@ExtendWith(MockitoExtension.class)
class MiReportSystemGeneratorDelegatorTest {

	@InjectMocks
	private MiReportSystemGeneratorDelegator cut;

	@Mock
	private MiReportEntityManagerResolver miReportEntityManagerResolver;

	@Mock
	private MiReportSystemRepository miReportSystemRepository;

	@Test
	void generateReport() {
		CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
		ExecutedRequestActionsMiReportParams reportParams = ExecutedRequestActionsMiReportParams.builder()
				.reportType(MiReportSystemType.COMPLETED_WORK).build();
		
		ExecutedRequestActionsMiReportResult<ExecutedRequestAction> result = ExecutedRequestActionsMiReportResult.builder()
				.reportType(MiReportSystemType.COMPLETED_WORK)
				.build();

		MiReportSystemGenerator<ExecutedRequestActionsMiReportParams> generator1 = new ExecutedRequestActionsReportGenerator<ExecutedRequestAction>() {
			@Override
			public List<ExecutedRequestAction> findExecutedRequestActions(EntityManager entityManager,
					ExecutedRequestActionsMiReportParams reportParams) {
				return null;
			}

			@Override
			public List<String> getColumnNames() {
				return null;
			}

			public String getReportType() {
				return "other";
			};
		};
		MiReportSystemGenerator<ExecutedRequestActionsMiReportParams> generator2 = new ExecutedRequestActionsReportGenerator<ExecutedRequestAction>() {
			@Override
			public List<ExecutedRequestAction> findExecutedRequestActions(EntityManager entityManager,
					ExecutedRequestActionsMiReportParams reportParams) {
				return null;
			}
			@Override
			public List<String> getColumnNames() {
				return null;
			}
			@Override
			public MiReportSystemResult generateMiReport(EntityManager entityManager,
					ExecutedRequestActionsMiReportParams reportParams) {
				return result;
			}
		};
		List miReportGenerators = List.of(generator1, generator2);

		
		when(miReportSystemRepository.findByCompetentAuthority(competentAuthority))
				.thenReturn(List.of(new MiReportSystemSearchResult() {
					@Override
					public String getMiReportType() {
						return MiReportSystemType.COMPLETED_WORK;
					}

					@Override
					public int getId() {
						return 0;
					}
				}));
		EntityManager em = Mockito.mock(EntityManager.class);
		
		when(miReportEntityManagerResolver.resolveByCA(competentAuthority)).thenReturn(em);
		
		var actualResult = cut.generateReport(competentAuthority, reportParams, miReportGenerators);
		assertThat(actualResult).isEqualTo(result);
		verify(miReportSystemRepository, times(1)).findByCompetentAuthority(competentAuthority);
		verify(miReportEntityManagerResolver, times(1)).resolveByCA(competentAuthority);
	}
}
