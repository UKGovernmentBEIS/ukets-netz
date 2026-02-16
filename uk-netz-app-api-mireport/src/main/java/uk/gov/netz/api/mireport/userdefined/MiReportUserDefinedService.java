package uk.gov.netz.api.mireport.userdefined;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.userdefined.custom.CustomMiReportQuery;

import java.util.Optional;

import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@Service
@AllArgsConstructor
public class MiReportUserDefinedService {

    private final MiReportUserDefinedRepository miReportUserDefinedRepository;
    private final MiReportUserDefinedGeneratorDelegator miReportUserDefinedGeneratorDelegator;
    private static final MiReportUserDefinedMapper MI_REPORT_USER_DEFINED_MAPPER = Mappers.getMapper(MiReportUserDefinedMapper.class);
    
	@Transactional(readOnly = true)
	public MiReportUserDefinedResults findAllByCA(CompetentAuthorityEnum competentAuthority, int pageNumber,
			int pageSize) {
		Page<MiReportUserDefinedInfoDTO> page = miReportUserDefinedRepository.findAllByCA(competentAuthority,
				getPageable(pageNumber, pageSize));
		return page.isEmpty() ? MiReportUserDefinedResults.emptyMiReportUserDefinedResults()
				: MiReportUserDefinedResults.builder().queries(page.getContent()).total(page.getTotalElements())
						.build();
	}
	
	@Transactional(readOnly = true)
    public MiReportUserDefinedDTO findById(Long miReportUserDefinedId) {
        return miReportUserDefinedRepository.findById(miReportUserDefinedId)
                .map(MI_REPORT_USER_DEFINED_MAPPER::toMiReportUserDefinedDTO)
                .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));
    }

    @Transactional
    public void create(String userId, CompetentAuthorityEnum competentAuthority, @Valid MiReportUserDefinedDTO miReportUserDefinedDTO) {
        Optional<Long> miReportIdWithSameName =
                miReportUserDefinedRepository.findIdByReportNameAndCA(miReportUserDefinedDTO.getReportName(), competentAuthority);

        if (miReportIdWithSameName.isPresent()) {
            throw new BusinessException(ErrorCode.MI_REPORT_NAME_EXISTS_FOR_CA);
        }

        final MiReportUserDefinedEntity entity = MI_REPORT_USER_DEFINED_MAPPER
				.toMiReportUserDefinedEntity(miReportUserDefinedDTO, competentAuthority, userId);
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

        MI_REPORT_USER_DEFINED_MAPPER.updateMiReportUserDefinedEntity(queryEntity, miReportUserDefinedDTO);

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
        return PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "reportName"));
    }
}
