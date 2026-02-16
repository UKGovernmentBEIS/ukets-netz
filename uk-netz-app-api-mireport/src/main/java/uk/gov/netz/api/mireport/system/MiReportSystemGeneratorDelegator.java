package uk.gov.netz.api.mireport.system;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.core.MiReportEntityManagerResolver;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MiReportSystemGeneratorDelegator {

	private final MiReportEntityManagerResolver miReportEntityManagerResolver;
    private final MiReportSystemRepository miReportSystemRepository;

    public <T extends MiReportSystemParams, U extends MiReportSystemGenerator<T>> MiReportSystemResult generateReport(CompetentAuthorityEnum competentAuthority, T reportSystemParams, 
    		List<U> miReportSystemGenerators) {
        return miReportSystemGenerators.stream()
                .filter(generator -> Objects.equals(generator.getReportType(), reportSystemParams.getReportType()))
                .findFirst()
                .filter(generator -> miReportSystemRepository.findByCompetentAuthority(competentAuthority)
                        .stream()
                        .anyMatch(miReportSearchResult -> Objects.equals(miReportSearchResult.getMiReportType(), reportSystemParams.getReportType())))
                .map(generator -> generator.generateMiReport(Optional.ofNullable(miReportEntityManagerResolver.resolveByCA(competentAuthority))
                                .orElseThrow(() -> new BusinessException(ErrorCode.MI_REPORT_TYPE_NOT_SUPPORTED, competentAuthority)),
                                reportSystemParams))
                .orElseThrow(() -> new BusinessException(ErrorCode.MI_REPORT_TYPE_NOT_SUPPORTED));
    }

}
