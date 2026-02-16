package uk.gov.netz.api.mireport.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiReportSystemService {

    private final MiReportSystemGeneratorDelegator miReportSystemGeneratorDelegator;
    private final MiReportSystemRepository miReportSystemRepository;
    private final List<MiReportSystemGenerator> miReportSystemGenerators;

    public List<MiReportSystemSearchResult> findByCompetentAuthority(CompetentAuthorityEnum competentAuthority) {
        return miReportSystemRepository.findByCompetentAuthority(competentAuthority);
    }

    @Transactional(readOnly = true)
    public MiReportSystemResult generateReport(CompetentAuthorityEnum competentAuthority, MiReportSystemParams reportParams) {
        return miReportSystemGeneratorDelegator.generateReport(competentAuthority, reportParams, miReportSystemGenerators);
    }
}
