package uk.gov.netz.api.mireport.userdefined.category;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class MiReportUserDefinedCategoryService {

    private final MiReportUserDefinedCategoryRepository miReportUserDefinedCategoryRepository;

    private static final MiReportUserDefinedCategoryMapper MI_REPORT_USER_DEFINED_CATEGORY_MAPPER =
            Mappers.getMapper(MiReportUserDefinedCategoryMapper.class);


    @Transactional(readOnly = true)
    public List<MiReportUserDefinedCategoryDTO> findAllEnabled() {
        return miReportUserDefinedCategoryRepository.findAllByEnabledTrueOrderByNameAsc().stream()
                .map(MI_REPORT_USER_DEFINED_CATEGORY_MAPPER::toMiReportUserDefinedCategoryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<MiReportUserDefinedCategoryEntity> getByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<MiReportUserDefinedCategoryEntity> miReportUserDefinedCategoryEntities =
                miReportUserDefinedCategoryRepository.findAllById(ids);
        if (miReportUserDefinedCategoryEntities.size() != ids.size()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return new HashSet<>(miReportUserDefinedCategoryEntities);
    }

}