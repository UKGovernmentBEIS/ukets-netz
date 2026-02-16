package uk.gov.netz.api.mireport.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class MiReportSystemRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private MiReportSystemRepository miReportRepository;

    @Test
    void findByCompetentAuthority() {
        CompetentAuthorityEnum[] competentAuthorities = CompetentAuthorityEnum.values();
        Set<String> reportNames = Set.of(MiReportSystemType.LIST_OF_ACCOUNTS_ASSIGNED_REGULATOR_SITE_CONTACTS,
                MiReportSystemType.REGULATOR_OUTSTANDING_REQUEST_TASKS,
                MiReportSystemType.COMPLETED_WORK,
                MiReportSystemType.LIST_OF_ACCOUNTS_USERS_CONTACTS);

        int index = 1;
        for (CompetentAuthorityEnum authority : competentAuthorities) {
            for (String miReportType : reportNames) {
                MiReportSystemEntity entity = MiReportSystemEntity.builder()
                        .id(index++)
                        .competentAuthority(authority)
                        .miReportType(miReportType)
                        .build();
                miReportRepository.save(entity);
            }
        }
        miReportRepository.flush();


        for (CompetentAuthorityEnum ca : competentAuthorities) {
            List<MiReportSystemSearchResult> result = miReportRepository.findByCompetentAuthority(ca);
            assertThat(result).hasSize(reportNames.size());
            Set<String> reportNamesReceived = result.stream().map(MiReportSystemSearchResult::getMiReportType).collect(Collectors.toSet());
            assertEquals(reportNames, reportNamesReceived);
        }
    }

}