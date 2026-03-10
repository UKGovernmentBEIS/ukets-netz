package uk.gov.netz.api.verificationbody.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.common.domain.EmissionTradingScheme;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.verificationbody.domain.VerificationBody;
import uk.gov.netz.api.verificationbody.domain.dto.VerificationBodyDTO;
import uk.gov.netz.api.verificationbody.domain.dto.VerificationBodyNameInfoDTO;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;
import uk.gov.netz.api.verificationbody.repository.VerificationBodyRepository;
import uk.gov.netz.api.verificationbody.transform.VerificationBodyMapper;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class VerificationBodyQueryService {

    private final VerificationBodyRepository verificationBodyRepository;
    private static final VerificationBodyMapper verificationBodyMapper = Mappers.getMapper(VerificationBodyMapper.class);

    public Optional<VerificationBodyDTO> findVerificationBodyById(Long verificationBodyId) {
        return verificationBodyRepository.findById(verificationBodyId)
            .map(verificationBodyMapper::toVerificationBodyDTO);
    }

    public VerificationBody getVerificationBodyById(Long verificationBodyId) {
        return verificationBodyRepository.findById(verificationBodyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public VerificationBodyDTO getVerificationBodyDTOById(Long verificationBodyId) {
        VerificationBody verificationBody = 
                verificationBodyRepository
                    .findByIdEagerEmissionTradingSchemes(verificationBodyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, verificationBodyId));
        return verificationBodyMapper.toVerificationBodyDTO(verificationBody);
    }
    
    public VerificationBodyNameInfoDTO getVerificationBodyNameInfoById(Long verificationBodyId) {
        VerificationBody vb = 
                verificationBodyRepository
                    .findById(verificationBodyId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        return verificationBodyMapper.toVerificationBodyNameInfoDTO(vb);
    }
    
    public List<VerificationBodyNameInfoDTO> getAllActiveVerificationBodiesAccreditedToEmissionTradingScheme(
            EmissionTradingScheme emissionTradingScheme) {
        return verificationBodyRepository.findActiveVerificationBodiesAccreditedToEmissionTradingScheme(emissionTradingScheme.getName());
    }
    
    public boolean existsVerificationBodyById(Long verificationBodyId) {
        return verificationBodyRepository.existsById(verificationBodyId);
    }

    public boolean existsActiveVerificationBodyById(Long verificationBodyId) {
        return verificationBodyRepository.existsByIdAndStatus(verificationBodyId, VerificationBodyStatus.ACTIVE);
    }

    public boolean existsNonDisabledVerificationBodyById(Long verificationBodyId) {
        return verificationBodyRepository.existsByIdAndStatusNot(verificationBodyId, VerificationBodyStatus.DISABLED);
    }
    
    public boolean isVerificationBodyAccreditedToEmissionTradingScheme(Long verificationBodyId, EmissionTradingScheme emissionTradingScheme) {
        return verificationBodyRepository.isVerificationBodyAccreditedToEmissionTradingScheme(verificationBodyId, emissionTradingScheme.getName());
    }
    
}
