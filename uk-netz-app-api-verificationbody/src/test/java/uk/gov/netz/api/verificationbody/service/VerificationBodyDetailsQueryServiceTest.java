package uk.gov.netz.api.verificationbody.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.verificationbody.domain.dto.AddressDTO;
import uk.gov.netz.api.verificationbody.domain.dto.VerificationBodyDTO;
import uk.gov.netz.api.verificationbody.domain.verificationbodydetails.VerificationBodyDetails;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VerificationBodyDetailsQueryServiceTest {

    @InjectMocks
    private VerificationBodyDetailsQueryService service;

    @Mock
    private VerificationBodyQueryService verificationBodyQueryService;

    @Test
    void getVerificationBodyDetails() {
        final Long vbId = 1L;
        final VerificationBodyDTO verificationBodyDTO = VerificationBodyDTO.builder()
                .id(vbId)
                .name("name")
                .accreditationReferenceNumber("accrRefNum")
                .status(VerificationBodyStatus.ACTIVE)
                .address(AddressDTO.builder()
                        .line1("line1")
                        .city("city")
                        .country("GR")
                        .postcode("postcode")
                        .build())
                .emissionTradingSchemes(Set.of(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME.getName()))
                .build();

        final VerificationBodyDetails expected = VerificationBodyDetails.builder()
                .name(verificationBodyDTO.getName())
                .accreditationReferenceNumber(verificationBodyDTO.getAccreditationReferenceNumber())
                .address(verificationBodyDTO.getAddress())
                .emissionTradingSchemes(verificationBodyDTO.getEmissionTradingSchemes())
                .build();

        when(verificationBodyQueryService.findVerificationBodyById(vbId)).thenReturn(Optional.of(verificationBodyDTO));

        // Invoke
        Optional<VerificationBodyDetails> actual = service.getVerificationBodyDetails(vbId);

        // Verify
        verify(verificationBodyQueryService, times(1)).findVerificationBodyById(vbId);
        assertThat(actual).isPresent();
        Assertions.assertEquals(expected, actual.get());
    }
}
