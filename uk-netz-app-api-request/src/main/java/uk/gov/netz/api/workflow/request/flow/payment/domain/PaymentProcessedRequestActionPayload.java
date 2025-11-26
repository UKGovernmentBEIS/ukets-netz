package uk.gov.netz.api.workflow.request.flow.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.workflow.payment.domain.enumeration.PaymentMethodType;
import uk.gov.netz.api.workflow.request.core.domain.RequestActionPayload;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentProcessedRequestActionPayload extends RequestActionPayload {

    private String paymentRefNum;

    private LocalDate paymentDate;
    private String paidByFullName;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethodType paymentMethod;

    private LocalDate receivedDate;
}
