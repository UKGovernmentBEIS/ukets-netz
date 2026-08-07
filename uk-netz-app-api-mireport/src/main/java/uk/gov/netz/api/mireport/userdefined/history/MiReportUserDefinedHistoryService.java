package uk.gov.netz.api.mireport.userdefined.history;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedEntity;

@Service
@RequiredArgsConstructor
public class MiReportUserDefinedHistoryService {

    private final MiReportUserDefinedHistoryRepository repository;
    private final MiReportUserDefinedHistoryMapper mapper;

    @Transactional
    public void recordCreate(AppUser appUser , MiReportUserDefinedEntity miReportUserDefinedEntity) {

        MiReportUserDefinedHistoryEntity miReportUserDefinedHistoryEntity = mapper.toMiReportUserDefinedHistoryEntity(
                miReportUserDefinedEntity, MiReportUserDefinedChangeType.CREATE, appUser.getFullName(), null);

        repository.save(miReportUserDefinedHistoryEntity);
    }

    @Transactional
    public void recordUpdate(AppUser appUser , MiReportUserDefinedEntity miReportUserDefinedEntity, String reasonForChange) {

        MiReportUserDefinedHistoryEntity miReportUserDefinedHistoryEntity = mapper.toMiReportUserDefinedHistoryEntity(
                miReportUserDefinedEntity, MiReportUserDefinedChangeType.UPDATE, appUser.getFullName(), reasonForChange);

        repository.save(miReportUserDefinedHistoryEntity);
    }

    @Transactional(readOnly = true)
    public MiReportUserDefinedHistoryResults findByMiReportUserDefinedId(Long miReportUserDefinedId,int pageNumber,
                                                                              int pageSize) {
        Page<MiReportUserDefinedHistoryEntity> page = repository.findByMiReportId(miReportUserDefinedId,
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "submissionDate")));

        return page.isEmpty() ? MiReportUserDefinedHistoryResults.emptyMiReportUserDefinedHistoryResults()
                : MiReportUserDefinedHistoryResults.builder()
                .results(page.getContent().stream().map(mapper::toMiReportUserDefinedHistoryDTO).toList())
                .total(page.getTotalElements())
                .build();
    }

}
