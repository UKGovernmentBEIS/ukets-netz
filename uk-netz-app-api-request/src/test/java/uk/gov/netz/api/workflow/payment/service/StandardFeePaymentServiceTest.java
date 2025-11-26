package uk.gov.netz.api.workflow.payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.payment.domain.PaymentFeeMethod;
import uk.gov.netz.api.workflow.payment.domain.enumeration.FeeMethodType;
import uk.gov.netz.api.workflow.payment.domain.enumeration.FeeType;
import uk.gov.netz.api.workflow.payment.repository.PaymentFeeMethodRepository;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.workflow.payment.domain.enumeration.FeeType.FIXED;

@ExtendWith(MockitoExtension.class)
class StandardFeePaymentServiceTest {

    @InjectMocks
    private StandardFeePaymentService standardFeePaymentService;

    @Mock
    private PaymentFeeMethodRepository paymentFeeMethodRepository;

    @Test
    void getAmount() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        RequestType requestType = RequestType.builder().id(1L).code("code").build();
        Request request = Request.builder()
            .type(requestType)
            .build();
        addCaResourceToRequest(competentAuthority, request);
        FeeMethodType feeMethodType = FeeMethodType.STANDARD;
        BigDecimal fixedFee = BigDecimal.valueOf(2500.55);
        Map<FeeType, BigDecimal> fees = new EnumMap<>(FeeType.class);
        fees.put(FIXED, fixedFee);
        PaymentFeeMethod paymentFeeMethod = PaymentFeeMethod.builder()
            .competentAuthority(competentAuthority)
            .requestType(requestType)
            .type(feeMethodType)
            .fees(fees)
            .build();

        when(paymentFeeMethodRepository.findByCompetentAuthorityAndRequestTypeAndType(competentAuthority, requestType, feeMethodType))
            .thenReturn(Optional.of(paymentFeeMethod));

        assertEquals(fixedFee, standardFeePaymentService.getAmount(request));

        verify(paymentFeeMethodRepository, times(1))
            .findByCompetentAuthorityAndRequestTypeAndType(competentAuthority, requestType, feeMethodType);
    }

    @Test
    void getAmount_not_found() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        RequestType requestType = RequestType.builder().id(1L).code("code").build();
        Request request = Request.builder()
            .type(requestType)
            .build();
        addCaResourceToRequest(competentAuthority, request);

        when(paymentFeeMethodRepository.findByCompetentAuthorityAndRequestTypeAndType(competentAuthority, requestType, FeeMethodType.STANDARD))
            .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () ->
            standardFeePaymentService.getAmount(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());

        verify(paymentFeeMethodRepository, times(1))
            .findByCompetentAuthorityAndRequestTypeAndType(competentAuthority, requestType, FeeMethodType.STANDARD);
    }

    @Test
    void getAmount_fee_not_configured() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        RequestType requestType = RequestType.builder().id(1L).code("code").build();
        Request request = Request.builder()
            .type(requestType)
            .build();
        addCaResourceToRequest(competentAuthority, request);
        PaymentFeeMethod paymentFeeMethod = PaymentFeeMethod.builder()
            .competentAuthority(competentAuthority)
            .requestType(requestType)
            .type(FeeMethodType.STANDARD)
            .build();

        when(paymentFeeMethodRepository.findByCompetentAuthorityAndRequestTypeAndType(competentAuthority, requestType, FeeMethodType.STANDARD))
            .thenReturn(Optional.of(paymentFeeMethod));

        BusinessException exception = assertThrows(BusinessException.class,
            () -> standardFeePaymentService.getAmount(request));

        assertEquals(ErrorCode.FEE_CONFIGURATION_NOT_EXIST, exception.getErrorCode());

        verify(paymentFeeMethodRepository, times(1))
            .findByCompetentAuthorityAndRequestTypeAndType(competentAuthority, requestType, FeeMethodType.STANDARD);
        verifyNoMoreInteractions(paymentFeeMethodRepository);
    }

    @Test
    void getFeeMethodType() {
        assertEquals(FeeMethodType.STANDARD, standardFeePaymentService.getFeeMethodType());
    }

    @Test
    void resolveFeeType() {
        Request request = Request.builder().build();
        assertEquals(FIXED, standardFeePaymentService.resolveFeeType(request));
    }
    
    private void addCaResourceToRequest(CompetentAuthorityEnum competentAuthority, Request request) {
		RequestResource caResource = RequestResource.builder()
				.resourceType(ResourceType.CA)
				.resourceId(competentAuthority.name())
				.request(request)
				.build();
        request.getRequestResources().add(caResource);
	}
}