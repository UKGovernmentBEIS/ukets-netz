package uk.gov.netz.api.mireport.userdefined;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryDTO;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryEntity;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryService;
import uk.gov.netz.api.mireport.userdefined.custom.CustomMiReportQuery;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@Service
@Validated
@AllArgsConstructor
public class MiReportUserDefinedService {

    private final MiReportUserDefinedRepository miReportUserDefinedRepository;
    private final MiReportUserDefinedCategoryService miReportUserDefinedCategoryService;
    private final MiReportUserDefinedGeneratorDelegator miReportUserDefinedGeneratorDelegator;
    private final MiReportUserDefinedMapper miReportUserDefinedMapper;

    @Transactional(readOnly = true)
    public MiReportUserDefinedResults findAllByCA(CompetentAuthorityEnum competentAuthority, int pageNumber,
                                                  int pageSize) {
        Page<MiReportUserDefinedEntity> page = miReportUserDefinedRepository.findAllByCompetentAuthority(competentAuthority,
                getPageable(pageNumber, pageSize));

        return page.isEmpty() ? MiReportUserDefinedResults.emptyMiReportUserDefinedResults()
                : MiReportUserDefinedResults.builder()
                    .queries(page.getContent().stream().map(miReportUserDefinedMapper::toMiReportUserDefinedInfoDTO).toList())
                    .total(page.getTotalElements())
                    .build();
    }

    @Transactional(readOnly = true)
    public MiReportUserDefinedResults findAllByCA(CompetentAuthorityEnum competentAuthority, int pageNumber,
                                                  int pageSize, Long categoryId, String searchTerm) {
        Page<MiReportUserDefinedEntity> page = miReportUserDefinedRepository.findAllByCompetentAuthorityAndFilters(competentAuthority,
                categoryId, QuerySearchUtils.toSearchPattern(searchTerm), getPageable(pageNumber, pageSize));

        return page.isEmpty() ? MiReportUserDefinedResults.emptyMiReportUserDefinedResults()
                : MiReportUserDefinedResults.builder()
                .queries(page.getContent().stream().map(miReportUserDefinedMapper::toMiReportUserDefinedInfoDTO).toList())
                .total(page.getTotalElements())
                .build();
    }
	
	@Transactional(readOnly = true)
    public MiReportUserDefinedDTO findById(Long miReportUserDefinedId) {
        return miReportUserDefinedRepository.findById(miReportUserDefinedId)
                .map(miReportUserDefinedMapper::toMiReportUserDefinedDTO)
                .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));
    }

    @Transactional
    public void create(String userId, CompetentAuthorityEnum competentAuthority, @Valid MiReportUserDefinedDTO miReportUserDefinedDTO) {
        Optional<Long> miReportIdWithSameName =
                miReportUserDefinedRepository.findIdByReportNameAndCA(miReportUserDefinedDTO.getReportName(), competentAuthority);

        if (miReportIdWithSameName.isPresent()) {
            throw new BusinessException(ErrorCode.MI_REPORT_NAME_EXISTS_FOR_CA);
        }

        Set<MiReportUserDefinedCategoryEntity> categories = miReportUserDefinedCategoryService.getByIds(extractCategoryIds(miReportUserDefinedDTO));

        final MiReportUserDefinedEntity entity = miReportUserDefinedMapper
				.toMiReportUserDefinedEntity(miReportUserDefinedDTO, categories,competentAuthority, userId);

        miReportUserDefinedRepository.save(entity);
    }

    @Transactional
    public void update(Long id, @Valid MiReportUserDefinedDTO miReportUserDefinedDTO) {
        final MiReportUserDefinedEntity queryEntity = miReportUserDefinedRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));

        Optional<Long> miReportIdWithSameName =
                miReportUserDefinedRepository.findIdByReportNameAndCA(miReportUserDefinedDTO.getReportName(), queryEntity.getCompetentAuthority());

        if (miReportIdWithSameName.map(existingId -> !existingId.equals(id)).orElse(Boolean.FALSE)) {
            throw new BusinessException(ErrorCode.MI_REPORT_NAME_EXISTS_FOR_CA);
        }

        Set<MiReportUserDefinedCategoryEntity> categories = miReportUserDefinedCategoryService.getByIds(extractCategoryIds(miReportUserDefinedDTO));

        miReportUserDefinedMapper.updateMiReportUserDefinedEntity(queryEntity, miReportUserDefinedDTO, categories);

        miReportUserDefinedRepository.save(queryEntity);
    }

    @Transactional
    public void delete(Long miReportUserDefinedId) {
    	miReportUserDefinedRepository.deleteById(miReportUserDefinedId);
    }

    @Transactional(readOnly = true)
    public MiReportUserDefinedResult generateReport(Long miReportUserDefinedId) {
        final MiReportUserDefinedEntity miReportEntity = miReportUserDefinedRepository.findById(miReportUserDefinedId)
                .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));

		return miReportUserDefinedGeneratorDelegator.generateReport(miReportEntity.getCompetentAuthority(),
				miReportEntity.getQueryDefinition());
    }
    
    @Transactional(readOnly = true)
    public MiReportUserDefinedResult generateCustomReport(CompetentAuthorityEnum competentAuthority, CustomMiReportQuery customQuery) {
        return miReportUserDefinedGeneratorDelegator.generateReport(competentAuthority, customQuery.getSqlQuery());
    }
    
    private Pageable getPageable(int page, int pageSize) {
        return PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "lastUpdatedOn"));
    }

    private Set<Long> extractCategoryIds(MiReportUserDefinedDTO dto) {
        if (dto.getCategories() == null) {
            return Set.of();
        }
        return dto.getCategories().stream()
                .map(MiReportUserDefinedCategoryDTO::getId)
                .collect(Collectors.toSet());
    }
}
