package uk.gov.netz.api.mireport.userdefined;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.core.MiReportEntityManagerResolver;

@Service
@RequiredArgsConstructor
public class MiReportUserDefinedGeneratorDelegator {

	private final MiReportEntityManagerResolver miReportEntityManagerResolver;
	private final MiReportUserDefinedGenerator miReportUserDefinedGenerator;

	public MiReportUserDefinedResult generateReport(CompetentAuthorityEnum competentAuthority, String sqlQuery) {
		return generateReport(competentAuthority, sqlQuery, null);
	}

	public MiReportUserDefinedResult generateReport(CompetentAuthorityEnum competentAuthority, String sqlQuery, Integer maxRows) {
		return miReportUserDefinedGenerator
				.generateMiReport(miReportEntityManagerResolver.resolveByCA(competentAuthority), sqlQuery, maxRows);
	}

	public void validateQuery(String sqlQuery) {
		miReportUserDefinedGenerator
				.validateQuery(miReportEntityManagerResolver.resolveAny(), sqlQuery);
	}

}
