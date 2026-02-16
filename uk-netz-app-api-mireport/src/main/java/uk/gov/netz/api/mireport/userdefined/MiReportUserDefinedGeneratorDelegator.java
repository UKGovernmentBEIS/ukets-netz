package uk.gov.netz.api.mireport.userdefined;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.core.MiReportEntityManagerResolver;

@Service
@RequiredArgsConstructor
class MiReportUserDefinedGeneratorDelegator {

	private final MiReportEntityManagerResolver miReportEntityManagerResolver;
	private final MiReportUserDefinedGenerator miReportUserDefinedGenerator;

	public MiReportUserDefinedResult generateReport(CompetentAuthorityEnum competentAuthority, String sqlQuery) {
		return miReportUserDefinedGenerator
				.generateMiReport(miReportEntityManagerResolver.resolveByCA(competentAuthority), sqlQuery);
	}

}
