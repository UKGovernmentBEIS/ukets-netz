package uk.gov.netz.api.verificationbody.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.domain.EmissionTradingScheme;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.verificationbody.domain.VerificationBody;
import uk.gov.netz.api.verificationbody.domain.dto.VerificationBodyDTO;
import uk.gov.netz.api.verificationbody.domain.dto.VerificationBodyNameInfoDTO;
import uk.gov.netz.api.verificationbody.repository.VerificationBodyRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationBodyQueryServiceTest {
    
    @InjectMocks
    private VerificationBodyQueryService service;
    
    @Mock
    private VerificationBodyRepository verificationBodyRepository;

    @Test
    void getVerificationBodyDTOById() {
        Long verificationBodyId = 1L;
        VerificationBody vb = VerificationBody.builder().name("vb").id(verificationBodyId).build();
        
        when(verificationBodyRepository.findByIdEagerEmissionTradingSchemes(verificationBodyId))
            .thenReturn(Optional.of(vb));
        
        VerificationBodyDTO result = service.getVerificationBodyDTOById(verificationBodyId);
        
        assertThat(result.getId()).isEqualTo(verificationBodyId);
        verify(verificationBodyRepository, times(1)).findByIdEagerEmissionTradingSchemes(verificationBodyId);
    }

    @Test
    void getVerificationBodyDTOById_not_found() {
        Long verificationBodyId = 1L;
        
        when(verificationBodyRepository.findByIdEagerEmissionTradingSchemes(verificationBodyId))
            .thenReturn(Optional.empty());
        
        BusinessException be = assertThrows(BusinessException.class, () ->
                service.getVerificationBodyDTOById(verificationBodyId));
        
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        
        verify(verificationBodyRepository, times(1)).findByIdEagerEmissionTradingSchemes(verificationBodyId);
    }

    @Test
    void getVerificationBodyById() {
        Long verificationBodyId = 1L;
        VerificationBody vb = VerificationBody.builder().name("vb").id(verificationBodyId).build();

        when(verificationBodyRepository.findById(verificationBodyId))
            .thenReturn(Optional.of(vb));

        VerificationBody result = service.getVerificationBodyById(verificationBodyId);

        assertThat(result).isEqualTo(vb);
        verify(verificationBodyRepository, times(1)).findById(verificationBodyId);
    }

    @Test
    void getVerificationBodyById_not_found() {
        Long verificationBodyId = 1L;

        when(verificationBodyRepository.findById(verificationBodyId))
            .thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () ->
            service.getVerificationBodyById(verificationBodyId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(verificationBodyRepository, times(1)).findById(verificationBodyId);
    }

    @Test
    void getVerificationBodyNameInfoById() {
        Long verificationBodyId = 1L;
        VerificationBody vb = VerificationBody.builder().name("vb").id(verificationBodyId).build();
        
        when(verificationBodyRepository.findById(verificationBodyId))
            .thenReturn(Optional.of(vb));
        
        VerificationBodyNameInfoDTO result = service.getVerificationBodyNameInfoById(verificationBodyId);
        
        assertThat(result.getId()).isEqualTo(verificationBodyId);
        assertThat(result.getName()).isEqualTo("vb");
        
        verify(verificationBodyRepository, times(1)).findById(verificationBodyId);
    }
    
    @Test
    void getVerificationBodyNameInfoById_db_not_found() {
        Long verificationBodyId = 1L;
        
        when(verificationBodyRepository.findById(verificationBodyId))
            .thenReturn(Optional.empty());
        
        BusinessException be = assertThrows(BusinessException.class, () ->
                service.getVerificationBodyNameInfoById(verificationBodyId));
        
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        
        verify(verificationBodyRepository, times(1)).findById(verificationBodyId);
    }
    
    @Test
    void getAllActiveVerificationBodiesAccreditedToEmissionTradingScheme() {
        EmissionTradingScheme ets = TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME;
        List<VerificationBodyNameInfoDTO> verificationBodies = List
                .of(VerificationBodyNameInfoDTO.builder().id(1L).name("1").build());
        
        when(verificationBodyRepository.findActiveVerificationBodiesAccreditedToEmissionTradingScheme(ets.getName()))
            .thenReturn(verificationBodies);
    
        List<VerificationBodyNameInfoDTO> result = service.getAllActiveVerificationBodiesAccreditedToEmissionTradingScheme(ets);
        assertThat(result).isEqualTo(verificationBodies);
        
        verify(verificationBodyRepository, times(1)).findActiveVerificationBodiesAccreditedToEmissionTradingScheme(ets.getName());
    }
    
    @Test
    void existsVerificationBodyById() {
        Long verificationBodyId = 1L;
        
        when(verificationBodyRepository.existsById(verificationBodyId))
            .thenReturn(true);
        
        boolean result = service.existsVerificationBodyById(verificationBodyId);
        
        assertThat(result).isTrue();
        verify(verificationBodyRepository, times(1)).existsById(verificationBodyId);
    }
    
    @Test
    void isVerificationBodyAccreditedToEmissionTradingScheme() {
        Long verificationBodyId = 1L;
        EmissionTradingScheme emissionTradingScheme = TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME;
        
        when(verificationBodyRepository.isVerificationBodyAccreditedToEmissionTradingScheme(verificationBodyId, emissionTradingScheme.getName()))
            .thenReturn(true);
        
        boolean result = service.isVerificationBodyAccreditedToEmissionTradingScheme(verificationBodyId, emissionTradingScheme);
        assertThat(result).isTrue();
        verify(verificationBodyRepository, times(1)).isVerificationBodyAccreditedToEmissionTradingScheme(verificationBodyId, emissionTradingScheme.getName());
    }

    @Test
    void findVerificationBodyById() {
        Long verificationBodyId = 1L;
        VerificationBody verificationBody = VerificationBody.builder().name("vb").id(verificationBodyId).build();

        when(verificationBodyRepository.findById(verificationBodyId)).thenReturn(Optional.of(verificationBody));

        Optional<VerificationBodyDTO> result = service.findVerificationBodyById(verificationBodyId);
        assertThat(result).isPresent();
        assertThat(result).isEqualTo(Optional.of(VerificationBodyDTO.builder().name("vb").id(verificationBodyId).build()));
        verify(verificationBodyRepository).findById(verificationBodyId);
    }

}
