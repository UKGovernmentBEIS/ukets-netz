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
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryDTO;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryEntity;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryService;
import uk.gov.netz.api.mireport.userdefined.custom.CustomMiReportQuery;
import uk.gov.netz.api.mireport.userdefined.favourite.MiReportUserDefinedFavouriteService;
import uk.gov.netz.api.mireport.userdefined.history.MiReportUserDefinedHistoryService;

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
    private final MiReportUserDefinedHistoryService miReportUserDefinedHistoryService;
    private final MiReportUserDefinedFavouriteService miReportUserDefinedFavouriteService;

    private static final int PREVIEW_ROW_LIMIT = 10;

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
    public MiReportUserDefinedResults findAllByCA(AppUser appUser, int pageNumber,
                                                  int pageSize, Long categoryId, String searchTerm, boolean favourites) {
        Page<MiReportUserDefinedEntity> page = miReportUserDefinedRepository.findAllByCompetentAuthorityAndFilters(appUser.getCompetentAuthority(),
                categoryId, QuerySearchUtils.toSearchPattern(searchTerm), favourites ? appUser.getUserId() : null, getPageable(pageNumber, pageSize));

        return page.isEmpty() ? MiReportUserDefinedResults.emptyMiReportUserDefinedResults()
                : MiReportUserDefinedResults.builder()
                .queries(page.getContent().stream().map(miReportUserDefinedMapper::toMiReportUserDefinedInfoDTO).toList())
                .total(page.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public MiReportUserDefinedDTO findById(AppUser appUser, Long miReportUserDefinedId) {
        MiReportUserDefinedEntity entity = miReportUserDefinedRepository.findById(miReportUserDefinedId)
                .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));

        return miReportUserDefinedMapper.toMiReportUserDefinedDTO(entity,
                miReportUserDefinedFavouriteService.isFavourite(appUser, miReportUserDefinedId));
    }

    @Transactional
    public void create(AppUser appUser, @Valid MiReportUserDefinedDTO miReportUserDefinedDTO) {
        CompetentAuthorityEnum competentAuthority = appUser.getCompetentAuthority();

        Optional<Long> miReportIdWithSameName =
                miReportUserDefinedRepository.findIdByReportNameAndCA(miReportUserDefinedDTO.getReportName(), competentAuthority);

        if (miReportIdWithSameName.isPresent()) {
            throw new BusinessException(ErrorCode.MI_REPORT_NAME_EXISTS_FOR_CA);
        }

        Set<MiReportUserDefinedCategoryEntity> categories = miReportUserDefinedCategoryService.getByIds(extractCategoryIds(miReportUserDefinedDTO));

        final MiReportUserDefinedEntity entity =
                miReportUserDefinedMapper.toMiReportUserDefinedEntity(miReportUserDefinedDTO, categories, competentAuthority, appUser.getUserId());

        MiReportUserDefinedEntity miReportUserDefinedEntity = miReportUserDefinedRepository.save(entity);

        miReportUserDefinedHistoryService.recordCreate(appUser, miReportUserDefinedEntity);

    }

    @Transactional
    public void update(Long id, AppUser appUser, @Valid MiReportUserDefinedUpdateDTO miReportUserDefinedUpdateDTO) {
        MiReportUserDefinedDTO miReportUserDefinedDTO = miReportUserDefinedUpdateDTO.getUserDefinedDTO();
        final MiReportUserDefinedEntity queryEntity = miReportUserDefinedRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));

        Optional<Long> miReportIdWithSameName =
                miReportUserDefinedRepository.findIdByReportNameAndCA(miReportUserDefinedDTO.getReportName(), queryEntity.getCompetentAuthority());

        if (miReportIdWithSameName.map(existingId -> !existingId.equals(id)).orElse(Boolean.FALSE)) {
            throw new BusinessException(ErrorCode.MI_REPORT_NAME_EXISTS_FOR_CA);
        }

        Set<MiReportUserDefinedCategoryEntity> categories = miReportUserDefinedCategoryService.getByIds(extractCategoryIds(miReportUserDefinedDTO));

        miReportUserDefinedMapper.updateMiReportUserDefinedEntity(queryEntity, miReportUserDefinedDTO, categories);

        MiReportUserDefinedEntity miReportUserDefinedEntity = miReportUserDefinedRepository.save(queryEntity);

        miReportUserDefinedHistoryService.recordUpdate(appUser, miReportUserDefinedEntity, miReportUserDefinedUpdateDTO.getReasonForChange());
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

    @Transactional(readOnly = true)
    public MiReportUserDefinedResult previewCustomReport(CompetentAuthorityEnum competentAuthority, CustomMiReportQuery customQuery) {
        return miReportUserDefinedGeneratorDelegator.generateReport(competentAuthority, customQuery.getSqlQuery(),PREVIEW_ROW_LIMIT);
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
