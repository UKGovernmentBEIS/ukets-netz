package uk.gov.netz.api.workflow.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.payment.domain.PaymentFeeMethod;
import uk.gov.netz.api.workflow.payment.domain.enumeration.FeeMethodType;
import uk.gov.netz.api.workflow.payment.repository.PaymentFeeMethodRepository;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentFeeMethodService {

    private final PaymentFeeMethodRepository paymentFeeMethodRepository;

    public Optional<FeeMethodType> getFeeMethodType(CompetentAuthorityEnum competentAuthority, RequestType requestType) {
        return paymentFeeMethodRepository.findByCompetentAuthorityAndRequestType(competentAuthority, requestType)
                .map(PaymentFeeMethod::getType)
                .or(Optional::empty);
    }
}
