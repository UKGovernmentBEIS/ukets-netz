package uk.gov.netz.api.workflow.payment.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.payment.domain.PaymentFeeMethod;
import uk.gov.netz.api.workflow.payment.domain.enumeration.FeeMethodType;
import uk.gov.netz.api.workflow.request.common.repository.RequestAbstractTest;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class PaymentFeeMethodRepositoryIT extends RequestAbstractTest {

    @Autowired
    private PaymentFeeMethodRepository repository;

    @Test
    void findByCompetentAuthorityAndRequestType() {
		RequestType requestType = createRequestType("code", "Descr", "processdef", "histcat", true, true, true, true, ResourceType.ACCOUNT);
    	
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        PaymentFeeMethod paymentFeeMethod = PaymentFeeMethod.builder()
            .competentAuthority(competentAuthority)
            .requestType(requestType)
            .type(FeeMethodType.STANDARD)
            .build();

        entityManager.persist(paymentFeeMethod);

        flushAndClear();

        Optional<PaymentFeeMethod> result =
            repository.findByCompetentAuthorityAndRequestType(competentAuthority, requestType);

        assertThat(result).isNotEmpty();
        assertEquals(paymentFeeMethod, result.get());
        assertThat(paymentFeeMethod.getRequestType()).isEqualTo(requestType);
    }

    @Test
    void findByCompetentAuthorityAndRequestType_different_ca_no_result() {
    	RequestType requestType = createRequestType("code", "Descr", "processdef", "histcat", true, true, true, true, ResourceType.ACCOUNT);
    	
        PaymentFeeMethod paymentFeeMethod = PaymentFeeMethod.builder()
            .competentAuthority(CompetentAuthorityEnum.SCOTLAND)
            .requestType(requestType)
            .type(FeeMethodType.STANDARD)
            .build();

        entityManager.persist(paymentFeeMethod);

        flushAndClear();

        Optional<PaymentFeeMethod> optionalResult =
            repository.findByCompetentAuthorityAndRequestType(CompetentAuthorityEnum.WALES, requestType);

        assertThat(optionalResult).isEmpty();
    }
    
    @Test
    void findByCompetentAuthorityAndRequestType_different_requestType_no_result() {
    	RequestType requestType = createRequestType("code", "Descr", "processdef", "histcat", true, true, true, true, ResourceType.ACCOUNT);
    	RequestType anotherRequestType = createRequestType("anotherRequestType", "Descr", "processdef2", "histcat", true, true, true, true, ResourceType.ACCOUNT);
    	
        PaymentFeeMethod paymentFeeMethod = PaymentFeeMethod.builder()
            .competentAuthority(CompetentAuthorityEnum.SCOTLAND)
            .requestType(requestType)
            .type(FeeMethodType.STANDARD)
            .build();

        entityManager.persist(paymentFeeMethod);

        flushAndClear();

        Optional<PaymentFeeMethod> optionalResult =
            repository.findByCompetentAuthorityAndRequestType(CompetentAuthorityEnum.SCOTLAND, anotherRequestType);

        assertThat(optionalResult).isEmpty();
    }

    @Test
    void findByCompetentAuthorityAndRequestTypeAndType() {
    	RequestType requestType = createRequestType("code", "Descr", "processdef", "histcat", true, true, true, true, ResourceType.ACCOUNT);

        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        FeeMethodType feeMethodType = FeeMethodType.STANDARD;
        PaymentFeeMethod paymentFeeMethod = PaymentFeeMethod.builder()
            .competentAuthority(competentAuthority)
            .requestType(requestType)
            .type(feeMethodType)
            .build();

        entityManager.persist(paymentFeeMethod);

        flushAndClear();

        Optional<PaymentFeeMethod> optionalResult =
            repository.findByCompetentAuthorityAndRequestTypeAndType(competentAuthority, requestType, feeMethodType);

        assertThat(optionalResult).isNotEmpty();
        assertEquals(paymentFeeMethod, optionalResult.get());
    }

    @Test
    void findByCompetentAuthorityAndRequestTypeAndType_no_result() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
    	RequestType requestType = createRequestType("code", "Descr", "processdef", "histcat", true, true, true, true, ResourceType.ACCOUNT);
        PaymentFeeMethod paymentFeeMethod = PaymentFeeMethod.builder()
            .competentAuthority(competentAuthority)
            .requestType(requestType)
            .type(FeeMethodType.STANDARD)
            .build();

        entityManager.persist(paymentFeeMethod);

        flushAndClear();

        Optional<PaymentFeeMethod> optionalResult =
            repository.findByCompetentAuthorityAndRequestTypeAndType(competentAuthority, requestType, FeeMethodType.INSTALLATION_CATEGORY_BASED);

        assertThat(optionalResult).isEmpty();
    }
    
}